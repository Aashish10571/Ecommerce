package com.ecommerce.backend.auth.mapper;

import com.ecommerce.backend.auth.dto.request.NewUserPayload;
import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.enums.AuthProvider;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    UserTokenPayload toDto(User user);

    @Mapping(target = "authProvider", source = "authProvider")
    User toEntity(NewUserPayload newUserPayload, AuthProvider authProvider);

}