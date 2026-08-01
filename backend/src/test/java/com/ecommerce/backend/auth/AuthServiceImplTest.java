package com.ecommerce.backend.auth;

import com.ecommerce.backend.auth.dto.request.*;
import com.ecommerce.backend.auth.dto.response.TokenResponsePayload;
import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.entity.VerificationCode;
import com.ecommerce.backend.auth.enums.AuthProvider;
import com.ecommerce.backend.auth.exception.*;
import com.ecommerce.backend.auth.google.GoogleTokenVerifier;
import com.ecommerce.backend.auth.mapper.AuthMapper;
import com.ecommerce.backend.auth.repository.UserRepository;
import com.ecommerce.backend.auth.repository.VerificationCodeRepository;
import com.ecommerce.backend.auth.service.impl.AuthServiceImpl;
import com.ecommerce.backend.integration.mail.MailService;
import com.ecommerce.backend.security.jwt.dtos.UserTokenPayload;
import com.ecommerce.backend.security.jwt.enums.Token;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthServiceImpl")
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private AuthMapper authMapper;
    @Mock private MailService mailService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private VerificationCodeRepository verificationCodeRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String USERNAME = "testUser";
    private static final String EMAIL = "test@gmail.com";
    private static final String RAW_PASSWORD = "test@1227";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String ACCESS_TOKEN = "access-token-abc";
    private static final String REFRESH_TOKEN = "refresh-token-xyz";

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
        @DisplayName("returns access + refresh tokens and emails a login alert on success")
        void loginUser_withValidCredentials_returnsTokensAndSendsAlert() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(authMapper.toDto(localUser)).thenReturn(userTokenPayload);
            when(jwtUtil.generateToken(Token.ACCESS_TOKEN, userTokenPayload)).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateToken(Token.REFRESH_TOKEN, userTokenPayload)).thenReturn(REFRESH_TOKEN);

            TokenResponsePayload responsePayload = authService.loginUser(requestPayload);

            assertEquals(ACCESS_TOKEN, responsePayload.accessToken());
            assertEquals(REFRESH_TOKEN, responsePayload.refreshToken());
            ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
            verify(mailService).sendMail(eq(EMAIL), subjectCaptor.capture(), anyString());
            assertEquals("New Login Detected", subjectCaptor.getValue());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when email doesn't exist")
        void loginUser_whenUserNotFound_throwsInvalidCredentialsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(requestPayload));
            verifyNoInteractions(passwordEncoder, jwtUtil, mailService);
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
            verifyNoInteractions(passwordEncoder, jwtUtil, mailService);
        }

        @Test
        @DisplayName("throws InvalidCredentialsException on password mismatch")
        void loginUser_whenPasswordDoesNotMatch_throwsInvalidCredentialsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(requestPayload));
            verifyNoInteractions(jwtUtil, mailService);
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
            when(jwtUtil.generateToken(Token.ACCESS_TOKEN, userTokenPayload)).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateToken(Token.REFRESH_TOKEN, userTokenPayload)).thenReturn(REFRESH_TOKEN);

            TokenResponsePayload responsePayload = authService.registerNewUser(requestPayload);

            assertEquals(ACCESS_TOKEN, responsePayload.accessToken());
            assertEquals(REFRESH_TOKEN, responsePayload.refreshToken());
            verify(userRepository).save(localUser);
            verify(mailService).sendMail(eq(EMAIL), eq("Welcome! Your Account Is Ready"), anyString());
        }

        @Test
        @DisplayName("throws UserAlreadyExistsException when email is taken, never saves")
        void registerNewUser_whenEmailTaken_throwsUserAlreadyExistsExceptionAndNeverSaves() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThrows(UserAlreadyExistsException.class, () -> authService.registerNewUser(requestPayload));
            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder, jwtUtil, mailService);
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
            when(jwtUtil.generateToken(Token.ACCESS_TOKEN, userTokenPayload)).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateToken(Token.REFRESH_TOKEN, userTokenPayload)).thenReturn(REFRESH_TOKEN);

            TokenResponsePayload result = authService.continueWithGoogle(googleRequest);

            assertEquals(ACCESS_TOKEN, result.accessToken());
            assertEquals(REFRESH_TOKEN, result.refreshToken());
            verify(userRepository, never()).save(any());
            verify(mailService).sendMail(eq(EMAIL), eq("New Login Detected"), anyString());
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
            when(jwtUtil.generateToken(Token.ACCESS_TOKEN, userTokenPayload)).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateToken(Token.REFRESH_TOKEN, userTokenPayload)).thenReturn(REFRESH_TOKEN);

            TokenResponsePayload result = authService.continueWithGoogle(googleRequest);

            assertEquals(ACCESS_TOKEN, result.accessToken());
            assertEquals(REFRESH_TOKEN, result.refreshToken());
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(EMAIL, userCaptor.getValue().getEmail());
            assertEquals(AuthProvider.GOOGLE, userCaptor.getValue().getAuthProvider());
            verify(mailService).sendMail(eq(EMAIL), eq("Welcome! Your Account Is Ready"), anyString());
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
        @DisplayName("purges old codes, saves a fresh one, and emails it when the user exists")
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
            verify(mailService).sendMail(eq(EMAIL), eq("Password Reset Code"), contains(saved.getCode()));
        }

        @Test
        @DisplayName("silently no-ops when the email doesn't belong to any user")
        void requestReset_whenUserDoesNotExist_doesNothing() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            authService.requestReset(resetRequest);

            verifyNoInteractions(verificationCodeRepository, mailService);
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
        @DisplayName("updates password, marks code used, and emails confirmation on success")
        void resetPassword_withValidCode_updatesPasswordAndInvalidatesCode() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(verificationCodeRepository.findByEmailAndCode(EMAIL, CODE)).thenReturn(Optional.of(validCode));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_ENCODED);

            authService.resetPassword(resetPayload);

            assertEquals(NEW_ENCODED, localUser.getPassword());
            assertTrue(validCode.isUsed());
            verify(userRepository).save(localUser);
            verify(verificationCodeRepository).save(validCode);
            verify(mailService).sendMail(eq(EMAIL), eq("Your Password Has Been Changed"), anyString());
        }

        @Test
        @DisplayName("throws UserNotFoundException when the email doesn't exist")
        void resetPassword_whenUserNotFound_throwsUserNotFoundException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> authService.resetPassword(resetPayload));
            verifyNoInteractions(verificationCodeRepository, mailService);
        }

        @Test
        @DisplayName("throws InvalidVerificationCodeException when the code doesn't match")
        void resetPassword_whenCodeNotFound_throwsInvalidVerificationCodeException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(localUser));
            when(verificationCodeRepository.findByEmailAndCode(EMAIL, CODE)).thenReturn(Optional.empty());

            assertThrows(InvalidVerificationCodeException.class, () -> authService.resetPassword(resetPayload));

            verify(userRepository, never()).save(any());
            verifyNoInteractions(mailService);
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
            verifyNoInteractions(mailService);
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
            verifyNoInteractions(mailService);
        }
    }
}
