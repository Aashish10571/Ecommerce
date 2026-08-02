package com.ecommerce.backend.profile.mapper;

import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.profile.dto.response.ProfileResponsePayload;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileResponsePayload toDto(User user) {
        return new ProfileResponsePayload(
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
