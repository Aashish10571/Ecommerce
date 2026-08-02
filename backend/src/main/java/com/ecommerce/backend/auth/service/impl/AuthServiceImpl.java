package com.ecommerce.backend.auth.service.impl;

import com.ecommerce.backend.auth.dto.request.*;
import com.ecommerce.backend.auth.dto.response.TokenResponsePayload;
import com.ecommerce.backend.auth.entity.RefreshToken;
import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.entity.VerificationCode;
import com.ecommerce.backend.auth.enums.AuthProvider;
import com.ecommerce.backend.auth.exception.*;
import com.ecommerce.backend.auth.google.GoogleTokenVerifier;
import com.ecommerce.backend.auth.mapper.AuthMapper;
import com.ecommerce.backend.auth.repository.RefreshTokenRepository;
import com.ecommerce.backend.auth.repository.UserRepository;
import com.ecommerce.backend.auth.repository.VerificationCodeRepository;
import com.ecommerce.backend.auth.service.AuthService;
import com.ecommerce.backend.integration.mail.publisher.MailEventPublisher;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import com.ecommerce.backend.security.jwt.enums.Token;
import com.ecommerce.backend.security.jwt.exceptions.TokenInvalidException;
import com.ecommerce.backend.security.jwt.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailEventPublisher mailEventPublisher;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    @Value("${jwt.token.refresh.expire.time}")
    private Long refreshTokenExpirationTime;

    private TokenResponsePayload issueTokenPair(UserTokenPayload payload) {
        UUID accessTokenId = UUID.randomUUID();

        String accessToken = jwtUtil.generateToken(Token.ACCESS_TOKEN, payload, accessTokenId);

        RefreshToken record = RefreshToken.builder()
                .userId(payload.userId())
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationTime / 1000))
                .build();

        RefreshToken savedRecord = refreshTokenRepository.save(record);

        String refreshToken = jwtUtil.generateToken(Token.REFRESH_TOKEN, payload, savedRecord.getId());

        return new TokenResponsePayload(accessToken, refreshToken);
    }

    @Override
    public TokenResponsePayload loginUser(LoginRequestPayload requestPayload) {
        User user = userRepository.findByEmail(requestPayload.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new AuthProviderException("Please login using Google");
        }

        if (!passwordEncoder.matches(requestPayload.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        UserTokenPayload payload = authMapper.toDto(user);
        TokenResponsePayload responsePayload = issueTokenPair(payload);

        String subject = "New Login Detected";

        String message = """
                        Hello,

                        Your account was successfully accessed.

                        If this was you, you can safely ignore this email.

                        If you do not recognize this login, please change your password immediately and review your account activity.

                        Regards
                        """;

        mailEventPublisher.publish(user.getEmail(), subject, message);

        return responsePayload;
    }

    @Override
    public TokenResponsePayload registerNewUser(SignupRequestPayload requestPayload) {
        if (userRepository.existsByEmail(requestPayload.email())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        NewUserPayload newUserPayload =
                new NewUserPayload(requestPayload.username(), requestPayload.email());

        User newUser = authMapper.toEntity(newUserPayload, AuthProvider.LOCAL);

        newUser.setPassword(passwordEncoder.encode(requestPayload.password()));

        User savedUser = userRepository.save(newUser);

        UserTokenPayload payload = authMapper.toDto(savedUser);
        TokenResponsePayload responsePayload = issueTokenPair(payload);

        String subject = "Welcome! Your Account Is Ready";

        String message = """
                        Hello,

                        Your account was successfully signed in.

                        If this was you, no further action is required.

                        If you do not recognize this activity, please secure your account immediately by changing your password.

                        Regards
                        """;

        mailEventPublisher.publish(savedUser.getEmail(), subject, message);

        return responsePayload;
    }

    @Override
    public TokenResponsePayload continueWithGoogle(GoogleLoginPayload requestPayload) {
        GoogleIdToken.Payload tokenPayload = googleTokenVerifier.verifyToken(requestPayload.googleToken());

        String username = (String) tokenPayload.get("name");
        String email = tokenPayload.getEmail();

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user = existingUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .email(email)
                        .username(username)
                        .authProvider(AuthProvider.GOOGLE)
                        .build()
        ));

        UserTokenPayload userTokenPayload = authMapper.toDto(user);
        TokenResponsePayload responsePayload = issueTokenPair(userTokenPayload);

        String subject;
        String message;

        if (existingUser.isEmpty()) {
            subject = "Welcome! Your Account Is Ready";
            message = """
                    Hello,

                    Your account has been successfully created and signed in using Google.

                    You can now access your account without creating a separate password.

                    If you do not recognize this activity, please secure your Google account immediately.

                    Regards
                    """;
        } else {
            subject = "New Login Detected";
            message = """
                    Hello,

                    Your account was successfully accessed.

                    If this was you, you can safely ignore this email.

                    If you do not recognize this login, please change your password immediately and review your account activity.

                    Regards
                    """;
        }

        mailEventPublisher.publish(user.getEmail(), subject, message);

        return responsePayload;
    }

    @Override
    public void requestReset(ResetRequestPayload requestPayload) {
        String email = requestPayload.email();

        userRepository.findByEmail(email).ifPresent(user -> {
            verificationCodeRepository.deleteByEmailAndUsed(email, false);

            String code = generateCode();

            verificationCodeRepository.save(VerificationCode.builder()
                    .email(email)
                    .code(code)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .used(false)
                    .build()
            );

            String subject = "Password Reset Code";

            String message = """
                        Hello,

                        Your password reset code is:

                        %s

                        This code is valid for 10 minutes.

                        If you did not request a password reset, you can safely ignore this email.

                        Regards
                        """.formatted(code);

            mailEventPublisher.publish(email, subject, message);
        });
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetPayload resetPayload) {
        String email = resetPayload.email();
        String code = resetPayload.code();
        String newPassword = resetPayload.newPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCode(email, code)
                .orElseThrow(() -> new InvalidVerificationCodeException("Invalid verification code"));

        if (verificationCode.isUsed()) {
            throw new InvalidVerificationCodeException("Verification code has already been used");
        }

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException("Verification code has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        String subject = "Your Password Has Been Changed";

        String message = """
                        Hello,

                        Your account password was successfully changed.

                        If you made this change, no further action is required.

                        If you do not recognize this activity, please secure your account immediately.

                        Regards
                        """;

        mailEventPublisher.publish(email, subject, message);
    }

    @Override
    public TokenResponsePayload refreshToken(TokenRefreshPayload requestPayload) {
        String oldRefreshToken = requestPayload.refreshToken();

        jwtUtil.validateToken(oldRefreshToken);

        UUID tokenId = jwtUtil.extractTokenId(oldRefreshToken);

        RefreshToken record = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenInvalidException("Refresh token not recognized"));

        if (record.isRevoked()) {
            throw new TokenInvalidException("Refresh token has been revoked");
        }

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidException("Refresh token has expired");
        }

        UserTokenPayload extractedPayload = jwtUtil.extractUserPayload(oldRefreshToken);

        User user = userRepository.findByEmail(extractedPayload.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserTokenPayload payload = authMapper.toDto(user);

        String newAccessToken = jwtUtil.generateToken(Token.ACCESS_TOKEN, payload, UUID.randomUUID());

        return new TokenResponsePayload(newAccessToken, oldRefreshToken);
    }

    @Override
    public void logoutUser(TokenRefreshPayload requestPayload) {
        String refreshToken = requestPayload.refreshToken();

        jwtUtil.validateToken(refreshToken);

        UUID tokenId = jwtUtil.extractTokenId(refreshToken);

        RefreshToken record = refreshTokenRepository.findById(tokenId).orElseThrow(() -> new TokenInvalidException("Refresh token not recognized"));

        record.setRevoked(true);
        refreshTokenRepository.save(record);
    }

    private String generateCode() {
        return String.valueOf(new SecureRandom().nextInt(900000) + 100000);
    }
}
