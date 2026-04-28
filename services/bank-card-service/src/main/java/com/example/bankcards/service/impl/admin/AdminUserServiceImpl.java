package com.example.bankcards.service.impl.admin;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.request.AdminUserSearchRequestDTO;
import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.mapstruct.PageResponseMapper;
import com.example.bankcards.mapstruct.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AdminUserService;
import com.example.bankcards.service.finder.UserFinder;
import com.example.bankcards.specification.AdminUserSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Реализация административного сервиса для управления пользователями.
 *
 * <p>Сервис содержит бизнес-логику для административных операций:
 * просмотр пользователей с фильтрацией и пагинацией, получение пользователя
 * по идентификатору, создание пользователя, обновление данных, изменение роли,
 * блокировка, активация и логическое удаление.</p>
 *
 * <p>Сервис также отвечает за проверку уникальности email и телефона,
 * кодирование пароля при создании пользователя и контроль допустимых
 * изменений статуса и роли.</p>
 */
@AllArgsConstructor
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserFinder userFinder;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PageResponseMapper pageResponseMapper;

    /**
     * Возвращает постраничный список пользователей для администратора.
     *
     * <p>Метод поддерживает фильтрацию через {@link AdminUserSearchRequestDTO}.
     * Фильтры преобразуются в {@link AdminUserSpecification}, после чего
     * применяются к запросу в базу данных вместе с параметрами пагинации.</p>
     *
     * @param request параметры поиска, фильтрации и пагинации
     * @return постраничный ответ со списком пользователей
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OneUserResponseDTO> getUsers(AdminUserSearchRequestDTO request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<User> usersPage = userRepository.findAll(
                AdminUserSpecification.from(request),
                pageable
        );

        return pageResponseMapper.toPageResponse(
                usersPage,
                userMapper::toOneResponseUser
        );
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * <p>Если пользователь не найден, {@link UserFinder} выбрасывает исключение.</p>
     *
     * @param id идентификатор пользователя
     * @return DTO с данными пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public OneUserResponseDTO getUserById(long id) {
        User user = userFinder.getByIdOrThrow(id);
        return userMapper.toOneResponseUser(user);
    }

    /**
     * Создаёт нового пользователя.
     *
     * <p>Перед созданием проверяется уникальность email и телефона.
     * Пароль сохраняется только в закодированном виде.</p>
     *
     * @param request DTO с данными для создания пользователя
     * @return DTO созданного пользователя
     */
    @Override
    @Transactional
    public OneUserResponseDTO createUser(CreateUserRequestDTO request) {
        validateUniqueFieldsForCreate(request.getEmail(), request.getPhone());

        User user = buildUser(request);

        User savedUser = userRepository.save(user);

        log.info("Пользователь успешно создан: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        return userMapper.toOneResponseUser(savedUser);
    }

    /**
     * Частично обновляет данные пользователя.
     *
     * <p>Если поле в запросе не передано, оно не изменяется.
     * Если новое значение совпадает со старым, обновление не выполняется.
     * Проверка уникальности email и телефона выполняется только тогда,
     * когда соответствующее значение действительно меняется.</p>
     *
     * @param id идентификатор пользователя
     * @param request DTO с новыми данными пользователя
     * @return DTO обновлённого пользователя
     */
    @Override
    @Transactional
    public OneUserResponseDTO updateUser(long id, UpdateUserRequestDTO request) {
        User user = userFinder.getByIdOrThrow(id);

        validateUniqueFieldsForUpdate(request, user);

        boolean changed = false;

        changed |= updateIfChanged(request.getEmail(), user.getEmail(), user::setEmail);
        changed |= updateIfChanged(request.getPhone(), user.getPhone(), user::setPhone);
        changed |= updateIfChanged(request.getFirstName(), user.getFirstName(), user::setFirstName);
        changed |= updateIfChanged(request.getLastName(), user.getLastName(), user::setLastName);

        if (changed) {
            user.setUpdatedAt(Instant.now());
        }

        return userMapper.toOneResponseUser(user);
    }

    /**
     * Обновляет роль пользователя.
     *
     * <p>Перед изменением проверяется, что пользователь не удалён
     * и что новая роль отличается от текущей.</p>
     *
     * @param id идентификатор пользователя
     * @param request DTO с новой ролью
     * @return DTO пользователя с обновлённой ролью
     */
    @Override
    @Transactional
    public OneUserResponseDTO updateUserRole(long id, UpdateUserRoleRequestDTO request) {
        User user = userFinder.getByIdOrThrow(id);

        validateUserCanUpdateRole(user, request.getRole());

        user.setRole(request.getRole());
        user.setUpdatedAt(Instant.now());

        log.info("Роль пользователя обновлена: role={}", request.getRole());

        return userMapper.toOneResponseUser(user);
    }

    /**
     * Блокирует пользователя.
     *
     * <p>Перед блокировкой проверяется, что пользователь не удалён
     * и ещё не находится в статусе {@link UserStatus#BLOCKED}.</p>
     *
     * @param id идентификатор пользователя
     * @return DTO заблокированного пользователя
     */
    @Override
    @Transactional
    public OneUserResponseDTO blockUser(long id) {
        User user = userFinder.getByIdOrThrow(id);

        validateUserCanBeBlocked(user);

        user.setStatus(UserStatus.BLOCKED);
        user.setUpdatedAt(Instant.now());

        log.info("Пользователь заблокирован: userId={}", user.getId());

        return userMapper.toOneResponseUser(user);
    }

    /**
     * Активирует пользователя.
     *
     * <p>Перед активацией проверяется, что пользователь не удалён
     * и ещё не находится в статусе {@link UserStatus#ACTIVE}.</p>
     *
     * @param id идентификатор пользователя
     * @return DTO активированного пользователя
     */
    @Override
    @Transactional
    public OneUserResponseDTO activateUser(long id) {
        User user = userFinder.getByIdOrThrow(id);

        validateUserCanBeActivated(user);

        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());

        log.info("Пользователь активирован: userId={}", user.getId());

        return userMapper.toOneResponseUser(user);
    }

    /**
     * Логически удаляет пользователя.
     *
     * <p>Физическое удаление из базы данных не выполняется.
     * Вместо этого заполняется поле {@code deletedAt}.</p>
     *
     * @param id идентификатор пользователя
     */
    @Override
    @Transactional
    public void deleteUser(long id) {
        User user = userFinder.getByIdOrThrow(id);
        checkUserNotDeleted(user);
        user.setDeletedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        log.info("Пользователь логически удален: userId={}", user.getId());
    }

    /**
     * Создаёт entity пользователя перед сохранением.
     *
     * <p>Метод маппит данные из DTO, кодирует пароль, устанавливает
     * начальный статус {@link UserStatus#ACTIVE}, а также даты создания
     * и обновления.</p>
     *
     * @param request DTO создания пользователя
     * @return заполненная entity пользователя
     */
    private User buildUser(CreateUserRequestDTO request) {
        Instant now = Instant.now();

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return user;
    }

    /**
     * Проверяет, что пользователя можно активировать.
     *
     * @param user пользователь для проверки
     * @throws ConflictException если пользователь удалён или уже активирован
     */
    private void validateUserCanBeActivated(User user) {
        checkUserNotDeleted(user);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ConflictException("Пользователь уже активирован!");
        }
    }

    /**
     * Проверяет, что пользователя можно заблокировать.
     *
     * @param user пользователь для проверки
     * @throws ConflictException если пользователь удалён или уже заблокирован
     */
    private void validateUserCanBeBlocked(User user) {
        checkUserNotDeleted(user);

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ConflictException("Пользователь уже заблокирован!");
        }
    }

    /**
     * Проверяет, что роль пользователя можно изменить.
     *
     * @param user пользователь для проверки
     * @param newRole новая роль
     * @throws ConflictException если пользователь удалён или у него уже установлена эта роль
     */
    private void validateUserCanUpdateRole(User user, Role newRole) {
        checkUserNotDeleted(user);

        if (user.getRole() == newRole) {
            throw new ConflictException("У пользователя уже установлена эта роль!");
        }
    }

    /**
     * Проверяет, что пользователь не был логически удалён.
     *
     * @param user пользователь для проверки
     * @throws ConflictException если пользователь уже удалён
     */
    private void checkUserNotDeleted(User user) {
        if (user.getDeletedAt() != null) {
            throw new ConflictException("Пользователь уже удалён");
        }
    }

    /**
     * Проверяет уникальность email и телефона при обновлении пользователя.
     *
     * <p>Проверка выполняется только для тех полей, которые переданы
     * и отличаются от текущих значений пользователя.</p>
     *
     * @param request DTO обновления пользователя
     * @param user текущий пользователь
     */
    private void validateUniqueFieldsForUpdate(UpdateUserRequestDTO request, User user) {
        if (isChanged(request.getEmail(), user.getEmail()) && isEmailExists(request.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        if (isChanged(request.getPhone(), user.getPhone()) && isPhoneExists(request.getPhone())) {
            throw new ConflictException("Пользователь с таким номером телефона уже существует");
        }
    }

    /**
     * Обновляет поле entity только если новое значение передано
     * и отличается от старого.
     *
     * @param newValue новое значение
     * @param oldValue текущее значение
     * @param setter setter для обновляемого поля
     * @param <T> тип значения
     * @return {@code true}, если поле было изменено
     */
    private <T> boolean updateIfChanged(T newValue, T oldValue, Consumer<T> setter) {
        if (isChanged(newValue, oldValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    /**
     * Проверяет, что новое значение передано и отличается от старого.
     *
     * @param newValue новое значение
     * @param oldValue старое значение
     * @return {@code true}, если значение изменилось
     */
    private boolean isChanged(Object newValue, Object oldValue) {
        return newValue != null && !Objects.equals(newValue, oldValue);
    }

    /**
     * Проверяет уникальность email и телефона при создании пользователя.
     *
     * @param email email нового пользователя
     * @param phone телефон нового пользователя
     * @throws ConflictException если email или телефон уже используются
     */
    private void validateUniqueFieldsForCreate(String email, String phone) {
        if (isEmailExists(email)) {
            log.warn("Попытка создать пользователя с уже занятым email");
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        if (isPhoneExists(phone)) {
            log.warn("Попытка создать пользователя с уже занятым номером телефона");
            throw new ConflictException("Пользователь с таким номером телефона уже существует");
        }
    }

    private boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean isPhoneExists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }
}