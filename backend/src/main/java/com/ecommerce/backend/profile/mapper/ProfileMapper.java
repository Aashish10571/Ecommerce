package com.ecommerce.backend.profile.mapper;

import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.profile.dto.response.ProfileResponsePayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponsePayload toDto(User user);
}