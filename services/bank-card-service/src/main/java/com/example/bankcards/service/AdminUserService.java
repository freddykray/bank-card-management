package com.example.bankcards.service;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.request.AdminUserSearchRequestDTO;
import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;

public interface AdminUserService {
    PageResponseDTO<OneUserResponseDTO> getUsers(AdminUserSearchRequestDTO request);
    OneUserResponseDTO getUserById(long id);
    OneUserResponseDTO createUser(CreateUserRequestDTO request);
    OneUserResponseDTO updateUser(long id, UpdateUserRequestDTO request);
    OneUserResponseDTO updateUserRole(long id, UpdateUserRoleRequestDTO request);
    OneUserResponseDTO blockUser(long id);
    OneUserResponseDTO activateUser(long id);
    void deleteUser(long id);
}