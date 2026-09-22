package com.cruz.financialcontrol.model.mapper;

import com.cruz.financialcontrol.model.dto.user.AuthResponseDTO;
import com.cruz.financialcontrol.model.dto.user.UpdateUserDTO;
import com.cruz.financialcontrol.model.dto.user.CreateUserDTO;
import com.cruz.financialcontrol.model.dto.user.UserResponseDTO;
import com.cruz.financialcontrol.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @IgnoreAuditFields
    User toEntity(CreateUserDTO createUserDTO);

    @IgnoreAuditFields
    @Mapping(target = "password", ignore = true)
    User toEntity(UpdateUserDTO updateUserDTO);

    UserResponseDTO toResponseDTO(User user);

    List<UserResponseDTO> toResponseDTO(List<User> users);
}
