package com.ecommerce.backend.auth.service;

import com.ecommerce.backend.auth.dto.request.*;
import com.ecommerce.backend.auth.dto.response.TokenResponsePayload;
import jakarta.transaction.Transactional;

public interface AuthService {
    TokenResponsePayload loginUser(LoginRequestPayload requestPayload);

    TokenResponsePayload registerNewUser(SignupRequestPayload requestPayload);

    TokenResponsePayload continueWithGoogle(GoogleLoginPayload requestPayload);

    void requestReset(ResetRequestPayload requestPayload);

    @Transactional
    void resetPassword(PasswordResetPayload resetPayload);

    TokenResponsePayload refreshToken(TokenRefreshPayload requestPayload);
}
