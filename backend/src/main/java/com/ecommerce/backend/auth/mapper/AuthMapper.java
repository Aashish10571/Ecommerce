package com.ecommerce.backend.auth.mapper;

import com.ecommerce.backend.auth.dto.request.NewUserPayload;
import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.enums.AuthProvider;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserTokenPayload toDto(User user) {
        return new UserTokenPayload(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }

    public User toEntity(NewUserPayload newUserPayload, AuthProvider authProvider) {
        if (newUserPayload == null) return null;

        User newUser = new User();
        newUser.setUsername(newUserPayload.username());
        newUser.setEmail(newUserPayload.email());
        newUser.setAuthProvider(authProvider);

        return newUser;
    }
}
