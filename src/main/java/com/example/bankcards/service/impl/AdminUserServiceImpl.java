package com.example.bankcards.service.impl;

import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.ListUserResponseDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.mapstruct.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AdminUserService;
import com.example.bankcards.service.finder.UserFinder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserFinder userFinder;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public ListUserResponseDTO getUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toListResponseUser(users);
    }

    @Override
    @Transactional(readOnly = true)
    public OneUserResponseDTO getUserById(long id) {
        User user = userFinder.getByIdOrThrow(id);
        return userMapper.toOneResponseUser(user);
    }

    @Override
    @Transactional
    public OneUserResponseDTO createUser(CreateUserRequestDTO request) {
        validateUniqueFields(request.getEmail(), request.getPhone(), null);

        Instant instant = Instant.now();

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(instant);
        user.setUpdatedAt(instant);
        User savedUser = userRepository.save(user);

        log.info("Создание пользователя id= {}", savedUser.getId());
        return userMapper.toOneResponseUser(savedUser);
    }

    @Override
    @Transactional
    public OneUserResponseDTO updateUser(long id, UpdateUserRequestDTO request) {
        User user = userFinder.getByIdOrThrow(id);

        validateUniqueFields(request.getEmail(), request.getPhone(), user);
        updateIfNotNull(request.getEmail(), user::setEmail);
        updateIfNotNull(request.getPhone(), user::setPhone);
        updateIfNotNull(request.getFirstName(), user::setFirstName);
        updateIfNotNull(request.getLastName(), user::setLastName);

        user.setUpdatedAt(Instant.now());
        return userMapper.toOneResponseUser(user);
    }

    @Override
    @Transactional
    public OneUserResponseDTO updateUserRole(long id, UpdateUserRoleRequestDTO request) {
        User user = userFinder.getByIdOrThrow(id);
        if (request.getRole() == user.getRole()) {
            throw new ConflictException("У пользователя уже установлена эта роль!");
        }
        user.setRole(request.getRole());
        user.setUpdatedAt(Instant.now());
        return userMapper.toOneResponseUser(user);
    }

    @Override
    @Transactional
    public OneUserResponseDTO blockUser(long id) {
        User user = userFinder.getByIdOrThrow(id);
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ConflictException("Пользователь уже заблокирован!");
        }
        user.setStatus(UserStatus.BLOCKED);
        user.setUpdatedAt(Instant.now());
        log.info("Пользователь заблокирован: userId={}", user.getId());

        return userMapper.toOneResponseUser(user);
    }

    @Override
    @Transactional
    public OneUserResponseDTO activateUser(long id) {
        User user = userFinder.getByIdOrThrow(id);
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ConflictException("Пользователь уже активирован!");
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());
        log.info("Пользователь активирован: userId={}", user.getId());

        return userMapper.toOneResponseUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        User user = userFinder.getByIdOrThrow(id);
        if (user.getDeletedAt() != null) {
            throw new ConflictException("Пользователь уже удален");
        }
        user.setDeletedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        log.info("Пользователь логически удален: userId={}", user.getId());
    }

    private void updateIfNotNull(String value, Consumer<String> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void validateUniqueFields(String email, String phone, User currentUser) {
        if (email != null && isEmailTakenByAnotherUser(email, currentUser)) {
            log.warn("Попытка использовать уже занятый email при обновлении/создании пользователя");
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        if (phone != null && isPhoneTakenByAnotherUser(phone, currentUser)) {
            log.warn("Попытка использовать уже занятый номер телефона при обновлении/создании пользователя");
            throw new ConflictException("Пользователь с таким номером телефона уже существует");
        }
    }

    private boolean isEmailTakenByAnotherUser(String email, User currentUser) {
        return userRepository.findByEmail(email)
                .filter(foundUser -> currentUser == null || !Objects.equals(foundUser.getId(), currentUser.getId()))
                .isPresent();
    }

    private boolean isPhoneTakenByAnotherUser(String phone, User currentUser) {
        return userRepository.findByPhone(phone)
                .filter(foundUser -> currentUser == null || !Objects.equals(foundUser.getId(), currentUser.getId()))
                .isPresent();
    }
}
