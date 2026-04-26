package com.example.bankcards.service;

import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.finder.UserFinder;
import com.example.bankcards.service.impl.AdminUserServiceImpl;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.bankcards.dto.admin.response.ListUserResponseDTO;
import com.example.bankcards.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserFinder userFinder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    void getUsers_success() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        List<User> users = List.of(user1, user2);

        ListUserResponseDTO responseDto = new ListUserResponseDTO();

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toListResponseUser(users)).thenReturn(responseDto);

        ListUserResponseDTO result = adminUserService.getUsers();

        assertEquals(responseDto, result);

        verify(userRepository).findAll();
        verify(userMapper).toListResponseUser(users);
    }

    @Test
    void createUser_success() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("user@example.com");
        request.setPassword("rawPassword123");
        request.setFirstName("Regular");
        request.setLastName("User");
        request.setPhone("+79990000002");
        request.setRole(Role.USER);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.getEmail());
        savedUser.setPhone(request.getPhone());
        savedUser.setRole(Role.USER);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setPassword("encodedPassword");

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(1L);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toOneResponseUser(savedUser)).thenReturn(responseDto);

        OneUserResponseDTO result = adminUserService.createUser(request);

        assertEquals(responseDto, result);
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByPhone(request.getPhone());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(user);
        verify(userMapper).toOneResponseUser(savedUser);
    }

    @Test
    void createUser_emailAlreadyExists_throwsConflictException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("user@example.com");
        request.setPassword("rawPassword123");
        request.setPhone("+79990000002");
        request.setRole(Role.USER);

        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setEmail(request.getEmail());

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.createUser(request)
        );

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository, never()).findByPhone(request.getPhone());
        verify(userMapper, never()).toEntity(any(CreateUserRequestDTO.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_phoneAlreadyExists_throwsConflictException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("user@example.com");
        request.setPassword("rawPassword123");
        request.setPhone("+79990000002");
        request.setRole(Role.USER);

        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setPhone(request.getPhone());

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhone(request.getPhone()))
                .thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.createUser(request)
        );

        assertEquals("Пользователь с таким номером телефона уже существует", exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByPhone(request.getPhone());
        verify(userMapper, never()).toEntity(any(CreateUserRequestDTO.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_success_changedFields() {
        long userId = 1L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail("new@example.com");
        request.setPhone("+79990000003");
        request.setFirstName("NewFirstName");
        request.setLastName("NewLastName");

        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");
        user.setPhone("+79990000002");
        user.setFirstName("OldFirstName");
        user.setLastName("OldLastName");

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(userMapper.toOneResponseUser(user)).thenReturn(responseDto);

        OneUserResponseDTO result = adminUserService.updateUser(userId, request);

        assertEquals(responseDto, result);
        assertEquals("new@example.com", user.getEmail());
        assertEquals("+79990000003", user.getPhone());
        assertEquals("NewFirstName", user.getFirstName());
        assertEquals("NewLastName", user.getLastName());
        assertNotNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).findByPhone("+79990000003");
        verify(userMapper).toOneResponseUser(user);
    }

    @Test
    void updateUser_sameFields_doesNotUpdateUpdatedAtAndDoesNotCheckUniqueFields() {
        long userId = 1L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail("user@example.com");
        request.setPhone("+79990000002");
        request.setFirstName("Fedor");
        request.setLastName("User");

        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        user.setPhone("+79990000002");
        user.setFirstName("Fedor");
        user.setLastName("User");

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userMapper.toOneResponseUser(user)).thenReturn(responseDto);

        OneUserResponseDTO result = adminUserService.updateUser(userId, request);

        assertEquals(responseDto, result);
        assertEquals("user@example.com", user.getEmail());
        assertEquals("+79990000002", user.getPhone());
        assertEquals("Fedor", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository, never()).findByEmail(any(String.class));
        verify(userRepository, never()).findByPhone(any(String.class));
        verify(userMapper).toOneResponseUser(user);
    }

    @Test
    void updateUser_changedEmailAlreadyExists_throwsConflictException() {
        long userId = 1L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail("taken@example.com");

        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("taken@example.com");

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.updateUser(userId, request)
        );

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        assertEquals("old@example.com", user.getEmail());
        assertNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository).findByEmail("taken@example.com");
        verify(userRepository, never()).findByPhone(any(String.class));
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void updateUser_changedPhoneAlreadyExists_throwsConflictException() {
        long userId = 1L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setPhone("+79990000003");

        User user = new User();
        user.setId(userId);
        user.setPhone("+79990000002");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setPhone("+79990000003");

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.findByPhone(request.getPhone())).thenReturn(Optional.of(existingUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.updateUser(userId, request)
        );

        assertEquals("Пользователь с таким номером телефона уже существует", exception.getMessage());
        assertEquals("+79990000002", user.getPhone());
        assertNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository).findByPhone("+79990000003");
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void updateUser_notFound_throwsNotFoundException() {
        long userId = 999L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail("new@example.com");

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminUserService.updateUser(userId, request)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository, never()).findByEmail(any(String.class));
        verify(userRepository, never()).findByPhone(any(String.class));
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void updateUserRole_success() {
        long userId = 1L;

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(Role.ADMIN);

        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userMapper.toOneResponseUser(user)).thenReturn(responseDto);

        OneUserResponseDTO result = adminUserService.updateUserRole(userId, request);

        assertEquals(responseDto, result);
        assertEquals(Role.ADMIN, user.getRole());
        assertNotNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper).toOneResponseUser(user);
    }

    @Test
    void updateUserRole_sameRole_throwsConflictException() {
        long userId = 1L;

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(Role.USER);

        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.updateUserRole(userId, request)
        );

        assertEquals("У пользователя уже установлена эта роль!", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void updateUserRole_deletedUser_throwsConflictException() {
        long userId = 1L;

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(Role.ADMIN);

        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);
        user.setDeletedAt(Instant.now());

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.updateUserRole(userId, request)
        );

        assertEquals("Пользователь уже удалён", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void updateUserRole_notFound_throwsNotFoundException() {
        long userId = 999L;

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(Role.ADMIN);

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminUserService.updateUserRole(userId, request)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void blockUser_success() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userMapper.toOneResponseUser(user)).thenReturn(responseDto);

        OneUserResponseDTO result = adminUserService.blockUser(userId);

        assertEquals(responseDto, result);
        assertEquals(UserStatus.BLOCKED, user.getStatus());
        assertNotNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper).toOneResponseUser(user);
    }

    @Test
    void blockUser_alreadyBlocked_throwsConflictException() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.BLOCKED);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.blockUser(userId)
        );

        assertEquals("Пользователь уже заблокирован!", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void blockUser_deletedUser_throwsConflictException() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setDeletedAt(Instant.now());

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.blockUser(userId)
        );

        assertEquals("Пользователь уже удалён", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void blockUser_notFound_throwsNotFoundException() {
        long userId = 999L;

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminUserService.blockUser(userId)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void activateUser_success() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.BLOCKED);

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(userMapper.toOneResponseUser(user)).thenReturn(responseDto);

        OneUserResponseDTO result = adminUserService.activateUser(userId);

        assertEquals(responseDto, result);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper).toOneResponseUser(user);
    }

    @Test
    void activateUser_alreadyActive_throwsConflictException() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.activateUser(userId)
        );

        assertEquals("Пользователь уже активирован!", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void activateUser_deletedUser_throwsConflictException() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.BLOCKED);
        user.setDeletedAt(Instant.now());

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.activateUser(userId)
        );

        assertEquals("Пользователь уже удалён", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void activateUser_notFound_throwsNotFoundException() {
        long userId = 999L;

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminUserService.activateUser(userId)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userMapper, never()).toOneResponseUser(any(User.class));
    }

    @Test
    void deleteUser_success_setsDeletedAt() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        adminUserService.deleteUser(userId);

        assertNotNull(user.getDeletedAt());
        assertNotNull(user.getUpdatedAt());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_alreadyDeleted_throwsConflictException() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setDeletedAt(Instant.now());

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminUserService.deleteUser(userId)
        );

        assertEquals("Пользователь уже удалён", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_notFound_throwsNotFoundException() {
        long userId = 999L;

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminUserService.deleteUser(userId)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}