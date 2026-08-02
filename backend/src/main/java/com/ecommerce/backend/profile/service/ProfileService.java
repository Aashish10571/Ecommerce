package com.ecommerce.backend.profile.service;

import com.ecommerce.backend.profile.dto.request.PasswordChangePayload;
import com.ecommerce.backend.profile.dto.request.UsernameChangePayload;
import com.ecommerce.backend.profile.dto.response.ProfileResponsePayload;

import java.util.UUID;

public interface ProfileService {
    ProfileResponsePayload getUserProfile(UUID userId);

    ProfileResponsePayload updateUsername(UUID userId, UsernameChangePayload requestPayload);

    void changePassword(UUID userId, PasswordChangePayload requestPayload);
}
