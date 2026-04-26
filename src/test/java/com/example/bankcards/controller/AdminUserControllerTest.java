package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.request.AdminUserSearchRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getList_success() throws Exception {
        PageResponseDTO<OneUserResponseDTO> responseDto =
                new PageResponseDTO<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0,
                        true,
                        true
                );

        when(adminUserService.getUsers(any(AdminUserSearchRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("email", "user@example.com")
                        .param("role", "USER")
                        .param("status", "ACTIVE")
                        .param("includeDeleted", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total_elements").value(0))
                .andExpect(jsonPath("$.total_pages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(adminUserService).getUsers(any(AdminUserSearchRequestDTO.class));
    }

    @Test
    void getUserById_success() throws Exception {
        long userId = 1L;

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(adminUserService.getUserById(userId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/admin/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(adminUserService).getUserById(userId);
    }

    @Test
    void createUser_success() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("user@example.com");
        request.setPassword("user123");
        request.setFirstName("Regular");
        request.setLastName("User");
        request.setPhone("+79990000002");
        request.setRole(Role.USER);

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(1L);

        when(adminUserService.createUser(any(CreateUserRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(adminUserService).createUser(any(CreateUserRequestDTO.class));
    }

    @Test
    void createUser_invalidRequest_returns400() throws Exception {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setPassword("user123");
        request.setFirstName("Regular");
        request.setLastName("User");
        request.setPhone("+79990000002");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).createUser(any(CreateUserRequestDTO.class));
    }

    @Test
    void updateUser_success() throws Exception {
        long userId = 1L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail("updated@example.com");
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setPhone("+79990000003");

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(adminUserService.updateUser(eq(userId), any(UpdateUserRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(adminUserService).updateUser(eq(userId), any(UpdateUserRequestDTO.class));
    }

    @Test
    void updateUser_invalidRequest_returns400() throws Exception {
        long userId = 1L;

        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setEmail("bad-email");
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setPhone("+79990000003");

        mockMvc.perform(patch("/api/admin/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never()).updateUser(eq(userId), any(UpdateUserRequestDTO.class));
    }

    @Test
    void updateUserRole_success() throws Exception {
        long userId = 1L;

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(Role.ADMIN);

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(adminUserService.updateUserRole(eq(userId), any(UpdateUserRoleRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(adminUserService).updateUserRole(eq(userId), any(UpdateUserRoleRequestDTO.class));
    }

    @Test
    void updateUserRole_invalidRequest_returns400() throws Exception {
        long userId = 1L;

        UpdateUserRoleRequestDTO request = new UpdateUserRoleRequestDTO();
        request.setRole(null);

        mockMvc.perform(patch("/api/admin/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .updateUserRole(eq(userId), any(UpdateUserRoleRequestDTO.class));
    }

    @Test
    void blockUser_success() throws Exception {
        long userId = 1L;

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(adminUserService.blockUser(userId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/users/{id}/block", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(adminUserService).blockUser(userId);
    }

    @Test
    void activateUser_success() throws Exception {
        long userId = 1L;

        OneUserResponseDTO responseDto = new OneUserResponseDTO();
        responseDto.setId(userId);

        when(adminUserService.activateUser(userId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/users/{id}/activate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));

        verify(adminUserService).activateUser(userId);
    }

    @Test
    void deleteUser_success() throws Exception {
        long userId = 1L;

        mockMvc.perform(delete("/api/admin/users/{id}", userId))
                .andExpect(status().isOk());

        verify(adminUserService).deleteUser(userId);
    }
}