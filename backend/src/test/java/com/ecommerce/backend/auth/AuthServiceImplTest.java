package com.ecommerce.backend.auth;

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
import com.ecommerce.backend.auth.service.impl.AuthServiceImpl;
import com.ecommerce.backend.integration.mail.publisher.MailEventPublisher;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import com.ecommerce.backend.security.jwt.enums.Token;
import com.ecommerce.backend.security.jwt.exceptions.TokenExpiredException;
import com.ecommerce.backend.security.jwt.exceptions.TokenInvalidException;
import com.ecommerce.backend.security.jwt.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthServiceImpl")
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private AuthMapper authMapper;
    @Mock private MailEventPublisher mailEventPublisher;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String USERNAME = "testUser";
    private static final String EMAIL = "test@gmail.com";
    private static final String RAW_PASSWORD = "test@1227";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String ACCESS_TOKEN = "access-token-abc";
    private static final String REFRESH_TOKEN = "refresh-token-xyz";
    private static final Long REFRESH_TOKEN_EXPIRATION_MS = 604_800_000L;

    private User localUser;
    private UserTokenPayload userTokenPayload;

    @BeforeEach
    void setUp() {
        localUser = User.builder()
                .username(USERNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .authProvider(AuthProvider.LOCAL)
                .build();

        userTokenPayload = mock(UserTokenPayload.class);

        ReflectionTestUtils.setField(authService, "refreshTokenExpirationTime", REFRESH_TOKEN_EXPIRATION_MS);
    }

    private void stubTokenGeneration(UserTokenPayload payload) {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken rt = invocation.getArgument(0);
            rt.setId(UUID.randomUUID());
            return rt;
        });
        when(jwtUtil.generateToken(eq(Token.ACCESS_TOKEN), eq(payload), any(UUID.class))).thenReturn(ACCESS_TOKEN);
        when(jwtUtil.generateToken(eq(Token.REFRESH_TOKEN), eq(payload), any(UUID.class))).thenReturn(REFRESH_TOKEN);
    }

    @Nested
    @DisplayName("loginUser")
    class LoginUser {

        private LoginRequestPayload requestPayload;

        @BeforeEach
        void init() {
            requestPayload = new LoginRequestPayload(EMAIL, RAW_PASSWORD);
        }

        @Test
        @DisplayName("returns access + refresh tokens and publishes a login alert event on success")
        void loginUser_withValidCredentials_returnsTokensAndSendsAlert() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(authMapper.toDto(localUser)).thenReturn(userTokenPayload);
            stubTokenGeneration(userTokenPayload);

            TokenResponsePayload responsePayload = authService.loginUser(requestPayload);

            assertEquals(ACCESS_TOKEN, responsePayload.accessToken());
            assertEquals(REFRESH_TOKEN, responsePayload.refreshToken());
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            verify(mailEventPublisher).publish(eq(EMAIL), subjectCaptor.capture(), anyString());
            assertEquals("New Login Detected", subjectCaptor.getValue());
            verify(refreshTokenRepository).save(any());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when email doesn't exist")
        void loginUser_whenUserNotFound_throwsInvalidCredentialsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(requestPayload));
            verifyNoInteractions(passwordEncoder, jwtUtil, mailEventPublisher, refreshTokenRepository);
        }

        @Test
        @DisplayName("throws AuthProviderException when the provider is google")
        void loginUser_whenAuthProviderIsGoogle_throwsAuthProviderException() {
            User googleUser = User.builder()
                    .username(USERNAME)
                    .email(EMAIL)
                    .authProvider(AuthProvider.GOOGLE)
                    .build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(googleUser));

            assertThrows(AuthProviderException.class, () -> authService.loginUser(requestPayload));
            verifyNoInteractions(passwordEncoder, jwtUtil, mailEventPublisher, refreshTokenRepository);
        }

        @Test
        @DisplayName("throws InvalidCredentialsException on password mismatch")
        void loginUser_whenPasswordDoesNotMatch_throwsInvalidCredentialsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(requestPayload));
            verifyNoInteractions(jwtUtil, mailEventPublisher, refreshTokenRepository);
        }
    }

    @Nested
    @DisplayName("registerNewUser")
    class RegisterNewUser {
        private SignupRequestPayload requestPayload;

        @BeforeEach
        void init() {
            requestPayload = new SignupRequestPayload(USERNAME, EMAIL, RAW_PASSWORD);
        }

        @Test
        @DisplayName("creates the user, encodes the password, and returns token on success")
        void registerNewUser_withNewEmail_createUserAndReturnsToken() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(authMapper.toEntity(any(NewUserPayload.class), eq(AuthProvider.LOCAL))).thenReturn(localUser);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(localUser)).thenReturn(localUser);
            when(authMapper.toDto(localUser)).thenReturn(userTokenPayload);
            stubTokenGeneration(userTokenPayload);

            TokenResponsePayload responsePayload = authService.registerNewUser(requestPayload);

            assertEquals(ACCESS_TOKEN, responsePayload.accessToken());
            assertEquals(REFRESH_TOKEN, responsePayload.refreshToken());
            verify(userRepository).save(localUser);
            verify(mailEventPublisher).publish(eq(EMAIL), eq("Welcome! Your Account Is Ready"), anyString());
            verify(refreshTokenRepository).save(any());
        }

        @Test
        @DisplayName("throws UserAlreadyExistsException when email is taken, never saves")
        void registerNewUser_whenEmailTaken_throwsUserAlreadyExistsExceptionAndNeverSaves() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThrows(UserAlreadyExistsException.class, () -> authService.registerNewUser(requestPayload));
            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder, jwtUtil, mailEventPublisher, refreshTokenRepository);
        }
    }

    @Nested
    @DisplayName("continueWithGoogle")
    class ContinueWithGoogle {

        private GoogleLoginPayload googleRequest;
        private GoogleIdToken.Payload googleTokenPayload;

        @BeforeEach
        void init() {
            googleRequest = new GoogleLoginPayload("valid-google-id-token");
            googleTokenPayload = mock(GoogleIdToken.Payload.class);

            when(googleTokenPayload.getEmail()).thenReturn(EMAIL);
            when(googleTokenPayload.get("name")).thenReturn(USERNAME);

            when(googleTokenVerifier.verifyToken("valid-google-id-token")).thenReturn(googleTokenPayload);
        }

        @Test
        @DisplayName("logs in an existing Google user without creating a new record")
        void continueWithGoogle_whenUserExists_logsInAndSendsLoginAlert() {
            User existingGoogleUser = User.builder()
                    .username(USERNAME)
                    .email(EMAIL)
                    .authProvider(AuthProvider.GOOGLE)
                    .build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingGoogleUser));
            when(authMapper.toDto(existingGoogleUser)).thenReturn(userTokenPayload);
            stubTokenGeneration(userTokenPayload);

            TokenResponsePayload result = authService.continueWithGoogle(googleRequest);

            assertEquals(ACCESS_TOKEN, result.accessToken());
            assertEquals(REFRESH_TOKEN, result.refreshToken());
            verify(userRepository, never()).save(any());
            verify(mailEventPublisher).publish(eq(EMAIL), eq("New Login Detected"), anyString());
        }

        @Test
        @DisplayName("provisions a new user and sends a welcome email when none exists")
        void continueWithGoogle_whenUserDoesNotExist_createsUserAndSendsWelcomeEmail() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            User newGoogleUser = User.builder()
                    .username(USERNAME)
                    .email(EMAIL)
                    .authProvider(AuthProvider.GOOGLE)
                    .build();
            when(userRepository.save(any(User.class))).thenReturn(newGoogleUser);
            when(authMapper.toDto(newGoogleUser)).thenReturn(userTokenPayload);
            stubTokenGeneration(userTokenPayload);

            TokenResponsePayload result = authService.continueWithGoogle(googleRequest);

            assertEquals(ACCESS_TOKEN, result.accessToken());
            assertEquals(REFRESH_TOKEN, result.refreshToken());
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(EMAIL, userCaptor.getValue().getEmail());
            assertEquals(AuthProvider.GOOGLE, userCaptor.getValue().getAuthProvider());
            verify(mailEventPublisher).publish(eq(EMAIL), eq("Welcome! Your Account Is Ready"), anyString());
        }
    }

    @Nested
    @DisplayName("requestReset")
    class RequestReset {

        private ResetRequestPayload resetRequest;

        @BeforeEach
        void init() {
            resetRequest = new ResetRequestPayload(EMAIL);
        }

        @Test
        @DisplayName("purges old codes, saves a fresh one, and publishes an event with it when the user exists")
        void requestReset_whenUserExists_generatesAndEmailsCode() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));

            authService.requestReset(resetRequest);

            verify(verificationCodeRepository).deleteByEmailAndUsed(EMAIL, false);
            ArgumentCaptor<VerificationCode> codeCaptor = ArgumentCaptor.forClass(VerificationCode.class);
            verify(verificationCodeRepository).save(codeCaptor.capture());
            VerificationCode saved = codeCaptor.getValue();
            assertEquals(EMAIL, saved.getEmail());
            assertFalse(saved.isUsed());
            assertNotNull(saved.getCode());
            assertEquals(6, saved.getCode().length());
            assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
            verify(mailEventPublisher).publish(eq(EMAIL), eq("Password Reset Code"), contains(saved.getCode()));
        }

        @Test
        @DisplayName("silently no-ops when the email doesn't belong to any user")
        void requestReset_whenUserDoesNotExist_doesNothing() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            authService.requestReset(resetRequest);

            verifyNoInteractions(verificationCodeRepository, mailEventPublisher);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        private static final String CODE = "123456";
        private static final String NEW_PASSWORD = "NewP@ss456";
        private static final String NEW_ENCODED = "$2a$10$newEncodedHash";

        private PasswordResetPayload resetPayload;
        private VerificationCode validCode;

        @BeforeEach
        void init() {
            resetPayload = new PasswordResetPayload(EMAIL, CODE, NEW_PASSWORD);

            validCode = VerificationCode.builder()
                    .email(EMAIL)
                    .code(CODE)
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();
        }

        @Test
        @DisplayName("updates password, marks code used, and publishes a confirmation event on success")
        void resetPassword_withValidCode_updatesPasswordAndInvalidatesCode() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(verificationCodeRepository.findByEmailAndCode(EMAIL, CODE)).thenReturn(Optional.of(validCode));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_ENCODED);

            authService.resetPassword(resetPayload);

            assertEquals(NEW_ENCODED, localUser.getPassword());
            assertTrue(validCode.isUsed());
            verify(userRepository).save(localUser);
            verify(verificationCodeRepository).save(validCode);
            verify(mailEventPublisher).publish(eq(EMAIL), eq("Your Password Has Been Changed"), anyString());
        }

        @Test
        @DisplayName("throws UserNotFoundException when the email doesn't exist")
        void resetPassword_whenUserNotFound_throwsUserNotFoundException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> authService.resetPassword(resetPayload));
            verifyNoInteractions(verificationCodeRepository, mailEventPublisher);
        }

        @Test
        @DisplayName("throws InvalidVerificationCodeException when the code doesn't match")
        void resetPassword_whenCodeNotFound_throwsInvalidVerificationCodeException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(verificationCodeRepository.findByEmailAndCode(EMAIL, CODE)).thenReturn(Optional.empty());

            assertThrows(InvalidVerificationCodeException.class, () -> authService.resetPassword(resetPayload));

            verify(userRepository, never()).save(any());
            verifyNoInteractions(mailEventPublisher);
        }

        @Test
        @DisplayName("throws InvalidVerificationCodeException when the code was already used")
        void resetPassword_whenCodeAlreadyUsed_throwsInvalidVerificationCodeException() {
            VerificationCode usedCode = VerificationCode.builder()
                    .email(EMAIL)
                    .code(CODE)
                    .used(true)
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(verificationCodeRepository.findByEmailAndCode(EMAIL, CODE))
                    .thenReturn(Optional.of(usedCode));

            assertThrows(InvalidVerificationCodeException.class,
                    () -> authService.resetPassword(resetPayload));

            verify(userRepository, never()).save(any());
            verifyNoInteractions(mailEventPublisher);
        }

        @Test
        @DisplayName("throws VerificationCodeExpiredException when the code has expired")
        void resetPassword_whenCodeExpired_throwsVerificationCodeExpiredException() {
            VerificationCode expiredCode = VerificationCode.builder()
                    .email(EMAIL)
                    .code(CODE)
                    .used(false)
                    .expiresAt(LocalDateTime.now().minusMinutes(1))
                    .build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(verificationCodeRepository.findByEmailAndCode(EMAIL, CODE))
                    .thenReturn(Optional.of(expiredCode));

            assertThrows(VerificationCodeExpiredException.class,
                    () -> authService.resetPassword(resetPayload));

            verify(userRepository, never()).save(any());
            verifyNoInteractions(mailEventPublisher);
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class TokenRefresh {

        private static final String OLD_REFRESH_TOKEN = "old-refresh-token-value";

        private TokenRefreshPayload refreshRequest;
        private UserTokenPayload extractedPayload;
        private UUID tokenId;
        private RefreshToken existingRecord;

        @BeforeEach
        void init() {
            refreshRequest = new TokenRefreshPayload(OLD_REFRESH_TOKEN);
            extractedPayload = mock(UserTokenPayload.class);
            tokenId = UUID.randomUUID();

            existingRecord = RefreshToken.builder()
                    .id(tokenId)
                    .userId(UUID.randomUUID())
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
        }

        @Test
        @DisplayName("issues a new access token but keeps the same refresh token when valid")
        void refreshToken_withValidToken_returnsNewAccessTokenSameRefreshToken() {
            when(jwtUtil.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.extractTokenId(OLD_REFRESH_TOKEN)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(existingRecord));
            when(jwtUtil.extractUserPayload(OLD_REFRESH_TOKEN)).thenReturn(extractedPayload);
            when(extractedPayload.email()).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(authMapper.toDto(localUser)).thenReturn(userTokenPayload);
            when(jwtUtil.generateToken(eq(Token.ACCESS_TOKEN), eq(userTokenPayload), any(UUID.class)))
                    .thenReturn(ACCESS_TOKEN);

            TokenResponsePayload result = authService.refreshToken(refreshRequest);

            assertEquals(ACCESS_TOKEN, result.accessToken());
            assertEquals(OLD_REFRESH_TOKEN, result.refreshToken());
            assertFalse(existingRecord.isRevoked(), "existing refresh token record should NOT be revoked");
            verify(jwtUtil).validateToken(OLD_REFRESH_TOKEN);
            verify(jwtUtil, never()).generateToken(eq(Token.REFRESH_TOKEN), any(), any());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("propagates the exception when the refresh token is expired/invalid")
        void refreshToken_whenTokenValidationFails_propagatesException() {
            when(jwtUtil.validateToken(OLD_REFRESH_TOKEN)).thenThrow(new TokenExpiredException("Authentication token has expired"));

            assertThrows(TokenExpiredException.class, () -> authService.refreshToken(refreshRequest));
            verify(jwtUtil, never()).extractTokenId(anyString());
            verify(jwtUtil, never()).extractUserPayload(anyString());
            verifyNoInteractions(userRepository, authMapper, refreshTokenRepository);
            verify(jwtUtil, never()).generateToken(any(), any(), any());
        }

        @Test
        @DisplayName("throws TokenInvalidException when the refresh token record isn't found")
        void refreshToken_whenRecordNotFound_throwsTokenInvalidException() {
            when(jwtUtil.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.extractTokenId(OLD_REFRESH_TOKEN)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

            assertThrows(TokenInvalidException.class, () -> authService.refreshToken(refreshRequest));
            verify(jwtUtil, never()).extractUserPayload(anyString());
            verifyNoInteractions(userRepository, authMapper);
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws TokenInvalidException when the refresh token record has been revoked")
        void refreshToken_whenRecordAlreadyRevoked_throwsTokenInvalidException() {
            existingRecord.setRevoked(true);
            when(jwtUtil.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.extractTokenId(OLD_REFRESH_TOKEN)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(existingRecord));

            assertThrows(TokenInvalidException.class, () -> authService.refreshToken(refreshRequest));
            verify(jwtUtil, never()).extractUserPayload(anyString());
            verifyNoInteractions(userRepository, authMapper);
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws TokenInvalidException when the refresh token record has expired")
        void refreshToken_whenRecordExpired_throwsTokenInvalidException() {
            existingRecord.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            when(jwtUtil.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.extractTokenId(OLD_REFRESH_TOKEN)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(existingRecord));

            assertThrows(TokenInvalidException.class, () -> authService.refreshToken(refreshRequest));
            verify(jwtUtil, never()).extractUserPayload(anyString());
            verifyNoInteractions(userRepository, authMapper);
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws UserNotFoundException when the token's user no longer exists")
        void refreshToken_whenUserNoLongerExists_throwsUserNotFoundException() {
            when(jwtUtil.validateToken(OLD_REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.extractTokenId(OLD_REFRESH_TOKEN)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(existingRecord));
            when(jwtUtil.extractUserPayload(OLD_REFRESH_TOKEN)).thenReturn(extractedPayload);
            when(extractedPayload.email()).thenReturn(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> authService.refreshToken(refreshRequest));
            verify(authMapper, never()).toDto(any());
            verify(jwtUtil, never()).generateToken(any(), any(), any());
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("logoutUser")
    class LogoutUser {

        private static final String REFRESH_TOKEN_VALUE = "refresh-token-to-revoke";

        private TokenRefreshPayload logoutRequest;
        private UUID tokenId;
        private RefreshToken existingRecord;

        @BeforeEach
        void init() {
            logoutRequest = new TokenRefreshPayload(REFRESH_TOKEN_VALUE);
            tokenId = UUID.randomUUID();

            existingRecord = RefreshToken.builder()
                    .id(tokenId)
                    .userId(UUID.randomUUID())
                    .revoked(false)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
        }

        @Test
        @DisplayName("revokes the matching refresh token record")
        void logoutUser_withValidToken_revokesRecord() {
            when(jwtUtil.validateToken(REFRESH_TOKEN_VALUE)).thenReturn(true);
            when(jwtUtil.extractTokenId(REFRESH_TOKEN_VALUE)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(existingRecord));

            authService.logoutUser(logoutRequest);

            assertTrue(existingRecord.isRevoked());
            verify(refreshTokenRepository).save(existingRecord);
        }

        @Test
        @DisplayName("throws TokenInvalidException when the refresh token record isn't found")
        void logoutUser_whenRecordNotFound_throwsTokenInvalidException() {
            when(jwtUtil.validateToken(REFRESH_TOKEN_VALUE)).thenReturn(true);
            when(jwtUtil.extractTokenId(REFRESH_TOKEN_VALUE)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

            assertThrows(TokenInvalidException.class, () -> authService.logoutUser(logoutRequest));
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("propagates the exception when the refresh token is expired/invalid")
        void logoutUser_whenTokenValidationFails_propagatesException() {
            when(jwtUtil.validateToken(REFRESH_TOKEN_VALUE))
                    .thenThrow(new TokenExpiredException("Authentication token has expired"));

            assertThrows(TokenExpiredException.class, () -> authService.logoutUser(logoutRequest));
            verify(jwtUtil, never()).extractTokenId(anyString());
            verifyNoInteractions(refreshTokenRepository);
        }

        @Test
        @DisplayName("revoking an already-revoked record does not throw")
        void logoutUser_whenRecordAlreadyRevoked_stillSavesWithoutError() {
            existingRecord.setRevoked(true);
            when(jwtUtil.validateToken(REFRESH_TOKEN_VALUE)).thenReturn(true);
            when(jwtUtil.extractTokenId(REFRESH_TOKEN_VALUE)).thenReturn(tokenId);
            when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(existingRecord));

            assertDoesNotThrow(() -> authService.logoutUser(logoutRequest));
            verify(refreshTokenRepository).save(existingRecord);
        }
    }
}