package com.example.bankcards.mapstruct;

import com.example.bankcards.dto.admin.response.ListUserResponseDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

    OneUserResponseDTO toOneResponseUser(User user);

    List<OneUserResponseDTO> toOneResponseUserList(List<User> users);

    default ListUserResponseDTO toListResponseUser(List<User> users) {
        List<OneUserResponseDTO> responseUsers = toOneResponseUserList(users);
        ListUserResponseDTO response = new ListUserResponseDTO();
        response.setUsers(responseUsers);
        response.setCount(responseUsers.size());
        return response;

    }
}
