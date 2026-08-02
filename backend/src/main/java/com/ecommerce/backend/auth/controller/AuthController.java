package com.ecommerce.backend.auth.controller;

import com.ecommerce.backend.auth.dto.request.*;
import com.ecommerce.backend.auth.dto.response.TokenResponsePayload;
import com.ecommerce.backend.auth.service.AuthService;
import com.ecommerce.backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponsePayload>> login(
            @Valid @RequestBody LoginRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        TokenResponsePayload responsePayload = authService.loginUser(requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        responsePayload,
                        request.getRequestURI())
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponsePayload>> signup(
            @Valid @RequestBody SignupRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        TokenResponsePayload responsePayload = authService.registerNewUser(requestPayload);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Account created successfully",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ResetRequestPayload requestPayload,
            HttpServletRequest request
    ) {
        authService.requestReset(requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification code sent successfully",
                        request.getRequestURI()
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody PasswordResetPayload resetPayload,
            HttpServletRequest request
    ) {
        authService.resetPassword(resetPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successful",
                        request.getRequestURI()
                )
        );
    }


    @PostMapping("/google")
    public ResponseEntity<ApiResponse<TokenResponsePayload>> googleLogin(
            @Valid @RequestBody GoogleLoginPayload requestPayload,
            HttpServletRequest request
    ) {
        TokenResponsePayload responsePayload = authService.continueWithGoogle(requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Google login successful",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponsePayload>> refresh(
            @RequestBody TokenRefreshPayload requestPayload,
            HttpServletRequest request
    ) {
        TokenResponsePayload responsePayload = authService.refreshToken(requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Token refreshed successfullly",
                        responsePayload,
                        request.getRequestURI()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @Valid @RequestBody TokenRefreshPayload requestPayload,
            HttpServletRequest request
    ) {
        authService.logoutUser(requestPayload);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successful",
                        request.getRequestURI()
                )
        );
    }
}
