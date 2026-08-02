package com.ecommerce.backend.profile.controller;

import com.ecommerce.backend.common.dto.ApiResponse;
import com.ecommerce.backend.profile.dto.request.PasswordChangePayload;
import com.ecommerce.backend.profile.dto.request.UsernameChangePayload;
import com.ecommerce.backend.profile.dto.response.ProfileResponsePayload;
import com.ecommerce.backend.profile.service.ProfileService;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponsePayload>> getProfile(
            @AuthenticationPrincipal UserTokenPayload principal,
            HttpServletRequest request
    ) {
        ProfileResponsePayload responsePayload = profileService.getUserProfile(principal.userId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile retrieved successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PatchMapping("/username")
    public ResponseEntity<ApiResponse<ProfileResponsePayload>> changeUsername(
            @AuthenticationPrincipal UserTokenPayload principal,
            @Valid @RequestBody UsernameChangePayload requestPayload,
            HttpServletRequest request
    ) {
        ProfileResponsePayload responsePayload = profileService.updateUsername(principal.userId(), requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile updated successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @AuthenticationPrincipal UserTokenPayload principal,
            @Valid @RequestBody PasswordChangePayload requestPayload,
            HttpServletRequest request
    ) {
        profileService.changePassword(principal.userId(), requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password changed successfully",
                        request.getRequestURI()
                )
        );
    }
}
