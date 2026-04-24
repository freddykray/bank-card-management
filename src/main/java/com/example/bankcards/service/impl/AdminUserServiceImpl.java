package com.example.bankcards.service.impl;

import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.ListUserResponseDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AdminUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public ListUserResponseDTO getUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toListResponseUser(users);
    }

    @Override
    public OneUserResponseDTO getUserById(long id) {
        User user = getUserByIdOrThrow(id);
        return userMapper.toOneResponseUser(user);
    }

    @Override
    public OneUserResponseDTO createUser(CreateUserRequestDTO request) {
        validateUniqueFields(request.getEmail(), request.getPhone(), null);

        Instant instant = Instant.now();
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(instant);
        user.setUpdatedAt(instant);

        User savedUser = userRepository.save(user);
        log.info("Создание пользователя id= {}", savedUser.getId());
        return userMapper.toOneResponseUser(savedUser);
    }

    @Override
    public OneUserResponseDTO updateUser(long id, UpdateUserRequestDTO request) {
        User user = getUserByIdOrThrow(id);

        validateUniqueFields(request.getEmail(), request.getPhone(), user);

        updateIfNotNull(request.getEmail(), user::setEmail);
        updateIfNotNull(request.getPhone(), user::setPhone);
        updateIfNotNull(request.getFirstName(), user::setFirstName);
        updateIfNotNull(request.getLastName(), user::setLastName);

        User savedUser = userRepository.save(user);
        return userMapper.toOneResponseUser(savedUser);
    }

    private void updateIfNotNull(String value, Consumer<String> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    @Override
    public OneUserResponseDTO updateUserRole(long id, UpdateUserRoleRequestDTO request) {
        return null;
    }

    @Override
    public OneUserResponseDTO blockUser(long id) {
        return null;
    }

    @Override
    public OneUserResponseDTO activateUser(long id) {
        return null;
    }

    @Override
    public void deleteUser(long id) {

    }

    private User getUserByIdOrThrow(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id= " + id));
    }

    private void validateUniqueFields(String email, String phone, User currentUser) {
        if (email != null && isEmailTakenByAnotherUser(email, currentUser)) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        if (phone != null && isPhoneTakenByAnotherUser(phone, currentUser)) {
            throw new ConflictException("Пользователь с таким номером телефона уже существует");
        }
    }

    private boolean isEmailTakenByAnotherUser(String email, User currentUser) {
        return userRepository.findByEmail(email)
                .filter(foundUser -> currentUser == null || foundUser.getId() != currentUser.getId())
                .isPresent();
    }

    private boolean isPhoneTakenByAnotherUser(String phone, User currentUser) {
        return userRepository.findByPhone(phone)
                .filter(foundUser -> currentUser == null || foundUser.getId() != currentUser.getId())
                .isPresent();
    }
}
