package com.example.bankcards.service.impl;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.request.UserTransferSearchRequestDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.PageResponseMapper;
import com.example.bankcards.mapstruct.TransferMapper;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.UserTransferService;
import com.example.bankcards.service.finder.CardFinder;
import com.example.bankcards.specification.UserTransferSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Реализация пользовательского сервиса для работы с переводами между картами.
 *
 * <p>Сервис отвечает за создание переводов между собственными картами пользователя,
 * получение истории переводов с фильтрацией и пагинацией, а также получение
 * конкретного перевода текущего пользователя.</p>
 *
 * <p>Все операции ограничены текущим авторизованным пользователем. Идентификатор
 * пользователя берётся из security context через {@link CurrentUserService}.
 * Пользователь не передаёт userId вручную и не может выполнить перевод с чужой карты
 * или получить доступ к чужим переводам.</p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserTransferServiceImpl implements UserTransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final CardFinder cardFinder;
    private final CurrentUserService currentUserService;
    private final PageResponseMapper pageResponseMapper;

    /**
     * Создаёт перевод между двумя картами текущего пользователя.
     *
     * <p>Перед выполнением перевода сервис получает текущего пользователя,
     * проверяет, что обе карты принадлежат ему, валидирует перевод, уменьшает
     * баланс карты списания и увеличивает баланс карты зачисления.</p>
     *
     * @param request DTO с идентификаторами карт и суммой перевода
     * @return DTO созданного перевода
     * @throws NotFoundException если одна из карт не найдена или не принадлежит текущему пользователю
     * @throws ConflictException если перевод нарушает бизнес-правила
     */
    @Override
    @Transactional
    public OneTransferResponseDTO createTransfer(CreateTransferRequestDTO request) {
        long currentUserId = currentUserService.getCurrentUserId();

        Card from = cardFinder.getByIdAndUserIdOrThrow(request.getFromCardId(), currentUserId);
        Card to = cardFinder.getByIdAndUserIdOrThrow(request.getToCardId(), currentUserId);

        BigDecimal amount = request.getAmount();

        validateTransfer(from, to, amount);

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transfer transfer = createTransferEntity(from, to, amount, TransferStatus.SUCCESS);

        log.info(
                "Перевод успешно выполнен: transferId={}, fromCardId={}, toCardId={}, amount={}",
                transfer.getId(),
                from.getId(),
                to.getId(),
                amount
        );

        return transferMapper.toTransferResponse(transfer);
    }

    /**
     * Возвращает постраничную историю переводов текущего пользователя.
     *
     * <p>Метод поддерживает фильтрацию через {@link UserTransferSearchRequestDTO}.
     * Фильтры применяются только к переводам, связанным с картами текущего пользователя.</p>
     *
     * @param request параметры поиска, фильтрации и пагинации
     * @return постраничный ответ со списком переводов
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OneTransferResponseDTO> getMyTransfers(UserTransferSearchRequestDTO request) {
        long currentUserId = currentUserService.getCurrentUserId();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Transfer> transfersPage = transferRepository.findAll(
                UserTransferSpecification.from(currentUserId, request),
                pageable
        );

        return pageResponseMapper.toPageResponse(
                transfersPage,
                transferMapper::toTransferResponse
        );
    }

    /**
     * Возвращает конкретный перевод текущего пользователя по идентификатору.
     *
     * @param id идентификатор перевода
     * @return DTO с данными перевода
     * @throws NotFoundException если перевод не найден или не принадлежит текущему пользователю
     */
    @Override
    @Transactional(readOnly = true)
    public OneTransferResponseDTO getMyTransferById(long id) {
        Transfer transfer = getTransferByIdAndCurrentUserIdOrThrow(id);
        return transferMapper.toTransferResponse(transfer);
    }

    /**
     * Ищет перевод по идентификатору и текущему пользователю.
     *
     * <p>Метод используется для защиты пользовательского endpoint'а:
     * пользователь может получить только свой перевод.</p>
     *
     * @param id идентификатор перевода
     * @return найденный перевод
     * @throws NotFoundException если перевод не найден или не принадлежит текущему пользователю
     */
    private Transfer getTransferByIdAndCurrentUserIdOrThrow(long id) {
        long currentUserId = currentUserService.getCurrentUserId();

        return transferRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new NotFoundException("Перевод не найден"));
    }

    /**
     * Проверяет бизнес-правила перевода.
     *
     * <p>Перевод запрещён, если карта списания и карта зачисления совпадают,
     * если одна из карт неактивна или если на карте списания недостаточно средств.</p>
     *
     * @param from карта списания
     * @param to карта зачисления
     * @param amount сумма перевода
     * @throws ConflictException если перевод невозможен
     */
    private void validateTransfer(Card from, Card to, BigDecimal amount) {
        if (Objects.equals(from.getId(), to.getId())) {
            log.warn("Попытка перевода на ту же самую карту: cardId={}", from.getId());
            throw new ConflictException("Нельзя выполнить перевод на ту же самую карту");
        }

        if (isCardUnavailableForTransfer(from) || isCardUnavailableForTransfer(to)) {
            log.warn(
                    "Попытка перевода между неактивными картами: fromCardId={}, fromStatus={}, toCardId={}, toStatus={}",
                    from.getId(),
                    from.getStatus(),
                    to.getId(),
                    to.getStatus()
            );
            throw new ConflictException("Перевод возможен только между активными картами");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            log.warn(
                    "Недостаточно средств для перевода: fromCardId={}, balance={}, amount={}",
                    from.getId(),
                    from.getBalance(),
                    amount
            );
            throw new ConflictException("Недостаточно средств для перевода");
        }
    }

    /**
     * Проверяет, что карта недоступна для перевода.
     *
     * @param card карта для проверки
     * @return {@code true}, если карта не находится в статусе {@link CardStatus#ACTIVE}
     */
    private boolean isCardUnavailableForTransfer(Card card) {
        return card.getStatus() != CardStatus.ACTIVE;
    }

    /**
     * Создаёт и сохраняет entity перевода.
     *
     * @param from карта списания
     * @param to карта зачисления
     * @param amount сумма перевода
     * @param status статус перевода
     * @return сохранённый перевод
     */
    private Transfer createTransferEntity(Card from, Card to, BigDecimal amount, TransferStatus status) {
        Transfer transfer = new Transfer();
        transfer.setFromCard(from);
        transfer.setToCard(to);
        transfer.setAmount(amount);
        transfer.setStatus(status);
        transfer.setCreatedAt(Instant.now());

        return transferRepository.save(transfer);
    }
}