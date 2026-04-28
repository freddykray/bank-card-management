package com.example.bankcards.service.impl;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.user.request.UserCardSearchRequestDTO;
import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.mapstruct.PageResponseMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.UserCardService;
import com.example.bankcards.specification.UserCardSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Реализация пользовательского сервиса для работы с банковскими картами.
 *
 * <p>Сервис отвечает за операции, доступные авторизованному пользователю:
 * просмотр своих карт, получение конкретной своей карты, просмотр баланса
 * и создание запроса на блокировку карты.</p>
 *
 * <p>Все операции ограничены текущим пользователем. Идентификатор пользователя
 * берётся из security context через {@link CurrentUserService}, поэтому клиент
 * не передаёт userId вручную и не может получить доступ к чужим картам.</p>
 */
@Service
@AllArgsConstructor
public class UserCardServiceImpl implements UserCardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CurrentUserService currentUserService;
    private final PageResponseMapper pageResponseMapper;

    /**
     * Возвращает постраничный список карт текущего пользователя.
     *
     * <p>Метод поддерживает фильтрацию через {@link UserCardSearchRequestDTO}.
     * Фильтры применяются только к картам текущего пользователя и только
     * к неудалённым картам.</p>
     *
     * @param request параметры поиска, фильтрации и пагинации
     * @return постраничный ответ со списком карт текущего пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<UserCardOneResponseDTO> getMyCards(UserCardSearchRequestDTO request) {
        long userId = currentUserService.getCurrentUserId();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Card> cardsPage = cardRepository.findAll(
                UserCardSpecification.from(userId, request),
                pageable
        );

        return pageResponseMapper.toPageResponse(
                cardsPage,
                cardMapper::toUserCardOneResponse
        );
    }

    /**
     * Возвращает конкретную карту текущего пользователя по идентификатору.
     *
     * <p>Карта должна принадлежать текущему пользователю и не должна быть
     * логически удалена.</p>
     *
     * @param id идентификатор карты
     * @return DTO с данными карты
     * @throws NotFoundException если карта не найдена или не принадлежит пользователю
     */
    @Override
    @Transactional(readOnly = true)
    public UserCardOneResponseDTO getMyCardById(long id) {
        Card card = getCardByIdAndUserIdAndDeletedAtIsNull(id);
        return cardMapper.toUserCardOneResponse(card);
    }

    /**
     * Возвращает баланс конкретной карты текущего пользователя.
     *
     * <p>Карта должна принадлежать текущему пользователю и не должна быть
     * логически удалена.</p>
     *
     * @param id идентификатор карты
     * @return DTO с балансом карты
     * @throws NotFoundException если карта не найдена или не принадлежит пользователю
     */
    @Override
    @Transactional(readOnly = true)
    public CardBalanceResponseDTO getMyCardBalance(long id) {
        Card card = getCardByIdAndUserIdAndDeletedAtIsNull(id);
        return cardMapper.toCardBalanceResponse(card);
    }

    /**
     * Создаёт запрос на блокировку карты текущего пользователя.
     *
     * <p>Метод не блокирует карту сразу, а только устанавливает признак
     * {@code blockRequested}. После этого администратор может увидеть заявку
     * через фильтр {@code blockRequested=true} и принять решение о блокировке.</p>
     *
     * @param id идентификатор карты
     * @return DTO карты с созданным запросом на блокировку
     * @throws NotFoundException если карта не найдена или не принадлежит пользователю
     * @throws ConflictException если карта уже заблокирована или запрос уже создан
     */
    @Override
    @Transactional
    public UserCardOneResponseDTO requestCardBlock(long id) {
        Card card = getCardByIdAndUserIdAndDeletedAtIsNull(id);
        checkBlockRequestAndBlockedCard(card);

        Instant now = Instant.now();

        card.setBlockRequested(true);
        card.setUpdatedAt(now);
        card.setBlockRequestedAt(now);

        return cardMapper.toUserCardOneResponse(card);
    }

    /**
     * Ищет карту по идентификатору, текущему пользователю и признаку неудалённости.
     *
     * <p>Метод используется во всех пользовательских операциях с конкретной картой,
     * чтобы гарантировать, что пользователь работает только со своими картами.</p>
     *
     * @param id идентификатор карты
     * @return найденная карта
     * @throws NotFoundException если карта не найдена, удалена или принадлежит другому пользователю
     */
    private Card getCardByIdAndUserIdAndDeletedAtIsNull(long id) {
        long userId = getUserIdFromContext();

        return cardRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена"));
    }

    /**
     * Возвращает идентификатор текущего авторизованного пользователя.
     *
     * @return id текущего пользователя
     */
    private long getUserIdFromContext() {
        return currentUserService.getCurrentUserId();
    }

    /**
     * Проверяет, что для карты можно создать запрос на блокировку.
     *
     * @param card карта для проверки
     * @throws ConflictException если карта уже заблокирована или запрос уже создан
     */
    private void checkBlockRequestAndBlockedCard(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ConflictException("Карта уже заблокирована");
        }

        if (card.isBlockRequested()) {
            throw new ConflictException("Запрос на блокировку карты уже создан");
        }
    }
}