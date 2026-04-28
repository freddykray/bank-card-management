package com.example.bankcards.service.impl;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.request.AdminCardSearchRequestDTO;
import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.GeneratedCardDetails;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.mapstruct.PageResponseMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.service.finder.CardFinder;
import com.example.bankcards.service.finder.UserFinder;
import com.example.bankcards.specification.AdminCardSpecification;
import com.example.bankcards.util.CardDetailsGenerator;
import com.example.bankcards.util.CardNumberEncryptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Реализация административного сервиса для управления банковскими картами.
 *
 * <p>Сервис содержит бизнес-логику для административных операций с картами:
 * просмотр списка карт с фильтрацией и пагинацией, получение карты по идентификатору,
 * создание карты, блокировка, активация и логическое удаление.</p>
 *
 * <p>При создании карты сервис не принимает номер карты и срок действия от клиента.
 * Эти данные генерируются внутри приложения через {@link CardDetailsGenerator}.
 * Полный номер карты шифруется перед сохранением, а наружу возвращается только
 * безопасное представление через DTO.</p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class AdminCardServiceImpl implements AdminCardService {

    private final CardRepository cardRepository;
    private final UserFinder userFinder;
    private final CardMapper cardMapper;
    private final CardNumberEncryptor encryptor;
    private final CardFinder cardFinder;
    private final CardDetailsGenerator cardDetailsGenerator;
    private final PageResponseMapper pageResponseMapper;

    /**
     * Возвращает постраничный список банковских карт для администратора.
     *
     * <p>Метод поддерживает фильтрацию через {@link AdminCardSearchRequestDTO}.
     * Фильтры преобразуются в {@link AdminCardSpecification}, после чего
     * применяются к запросу в базу данных вместе с пагинацией.</p>
     *
     * @param request параметры поиска, фильтрации и пагинации
     * @return постраничный ответ со списком карт
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OneCardResponseDTO> getCards(AdminCardSearchRequestDTO request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Card> cardsPage = cardRepository.findAll(
                AdminCardSpecification.from(request),
                pageable
        );

        return pageResponseMapper.toPageResponse(
                cardsPage,
                cardMapper::toAdminCardResponse
        );
    }

    /**
     * Возвращает банковскую карту по идентификатору.
     *
     * <p>Если карта не найдена, {@link CardFinder} выбрасывает исключение.</p>
     *
     * @param id идентификатор карты
     * @return DTO с данными карты
     */
    @Override
    @Transactional(readOnly = true)
    public OneCardResponseDTO getCardById(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        return cardMapper.toAdminCardResponse(card);
    }

    /**
     * Создаёт новую банковскую карту для указанного пользователя.
     *
     * <p>Пользователь определяется по {@code userId} из запроса.
     * Номер карты, hash, последние 4 цифры и срок действия генерируются
     * на стороне backend. Полный номер карты сохраняется в зашифрованном виде.</p>
     *
     * @param request DTO с данными для создания карты
     * @return DTO созданной карты
     */
    @Override
    @Transactional
    public OneCardResponseDTO createCard(CreateCardRequestDTO request) {
        User user = userFinder.getByIdOrThrow(request.getUserId());

        Card card = buildCard(request, user);

        Card savedCard = cardRepository.save(card);

        log.info("Карта создана: cardId={}, userId={}, last4={}",
                savedCard.getId(),
                user.getId(),
                savedCard.getCardNumberLast4()
        );

        return cardMapper.toAdminCardResponse(savedCard);
    }

    /**
     * Блокирует банковскую карту.
     *
     * <p>Перед блокировкой выполняется проверка, что карта не удалена
     * и ещё не находится в статусе {@link CardStatus#BLOCKED}.
     * После блокировки заявка пользователя на блокировку сбрасывается.</p>
     *
     * @param id идентификатор карты
     * @return DTO заблокированной карты
     */
    @Override
    @Transactional
    public OneCardResponseDTO blockCard(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        validateCardCanBeBlocked(card);
        card.setStatus(CardStatus.BLOCKED);
        card.setBlockRequested(false);
        card.setBlockRequestedAt(null);
        card.setUpdatedAt(Instant.now());
        log.info("Карта заблокирована: cardId={}", card.getId());
        return cardMapper.toAdminCardResponse(card);
    }

    /**
     * Активирует банковскую карту.
     *
     * <p>Перед активацией выполняется проверка, что карта не удалена,
     * не является уже активной и не имеет истёкший срок действия.</p>
     *
     * @param id идентификатор карты
     * @return DTO активированной карты
     */
    @Override
    @Transactional
    public OneCardResponseDTO activateCard(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        validateCardCanBeActivated(card);
        card.setStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(Instant.now());
        log.info("Карта активирована: cardId={}", card.getId());
        return cardMapper.toAdminCardResponse(card);
    }

    /**
     * Логически удаляет банковскую карту.
     *
     * <p>Физическое удаление из базы данных не выполняется.
     * Вместо этого заполняется поле {@code deletedAt}.</p>
     *
     * @param id идентификатор карты
     */
    @Override
    @Transactional
    public void deleteCard(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        checkCardNotDeleted(card);
        card.setDeletedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        log.info("Карта логически удалена: cardId={}", card.getId());
    }

    /**
     * Создаёт и заполняет entity карты перед сохранением.
     *
     * <p>Метод объединяет данные из запроса, найденного пользователя
     * и автоматически сгенерированные данные карты.</p>
     *
     * @param request DTO создания карты
     * @param user пользователь, которому создаётся карта
     * @return заполненная entity карты
     */
    private Card buildCard(CreateCardRequestDTO request, User user) {
        Instant now = Instant.now();

        GeneratedCardDetails generatedCardData =
                cardDetailsGenerator.generate();

        Card card = new Card();
        card.setEncryptedCardNumber(encryptor.encrypt(generatedCardData.cardNumber()));
        card.setCardNumberHash(generatedCardData.cardNumberHash());
        card.setCardNumberLast4(generatedCardData.last4());
        card.setOwnerName(request.getOwnerName());
        card.setExpirationDate(generatedCardData.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.getInitialBalance());
        card.setBlockRequested(false);
        card.setUser(user);
        card.setCreatedAt(now);
        card.setUpdatedAt(now);

        return card;
    }

    /**
     * Проверяет, что карту можно заблокировать.
     *
     * @param card карта для проверки
     * @throws ConflictException если карта удалена или уже заблокирована
     */
    private void validateCardCanBeBlocked(Card card) {
        checkCardNotDeleted(card);
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ConflictException("Карта уже заблокирована");
        }
    }

    /**
     * Проверяет, что карту можно активировать.
     *
     * @param card карта для проверки
     * @throws ConflictException если карта удалена, уже активна или имеет истёкший срок действия
     */
    private void validateCardCanBeActivated(Card card) {
        checkCardNotDeleted(card);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new ConflictException("Карта уже активна");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new ConflictException("Нельзя активировать карту с истёкшим сроком действия");
        }
    }

    /**
     * Проверяет, что карта не была логически удалена.
     *
     * @param card карта для проверки
     * @throws ConflictException если карта уже удалена
     */
    private void checkCardNotDeleted(Card card) {
        if (card.getDeletedAt() != null) {
            throw new ConflictException("Карта уже удалена");
        }
    }
}