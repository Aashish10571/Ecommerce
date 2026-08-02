package com.ecommerce.backend.profile;

import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.enums.AuthProvider;
import com.ecommerce.backend.auth.exception.InvalidCredentialsException;
import com.ecommerce.backend.auth.exception.UserNotFoundException;
import com.ecommerce.backend.auth.repository.UserRepository;
import com.ecommerce.backend.integration.mail.publisher.MailEventPublisher;
import com.ecommerce.backend.profile.dto.request.PasswordChangePayload;
import com.ecommerce.backend.profile.dto.request.UsernameChangePayload;
import com.ecommerce.backend.profile.dto.response.ProfileResponsePayload;
import com.ecommerce.backend.profile.mapper.ProfileMapper;
import com.ecommerce.backend.profile.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ProfileServiceImpl")
@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock private MailEventPublisher mailEventPublisher;
    @Mock private ProfileMapper profileMapper;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USERNAME = "testUser";
    private static final String EMAIL = "test@gmail.com";
    private static final String OLD_PASSWORD = "test@0817";

    private User localUser;

    @BeforeEach
    void setup() {
        localUser = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(OLD_PASSWORD)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("returns users profile when the user exists")
        void getProfile_userExists_returnsProfileInfo() {
            ProfileResponsePayload expectedResponse = new ProfileResponsePayload(
                    USERNAME,
                    EMAIL,
                    localUser.getCreatedAt()
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(localUser));
            when(profileMapper.toDto(localUser)).thenReturn(expectedResponse);

            ProfileResponsePayload responsePayload = profileService.getUserProfile(USER_ID);

            assertEquals(EMAIL, responsePayload.email());
            assertEquals(USERNAME, responsePayload.username());
            assertEquals(localUser.getCreatedAt(), responsePayload.createdAt());
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("throws UserNotFoundException when the user doesn't exist")
        void getProfile_userDoesNotExist_throwsUserNotFoundExceptionAndReturnsNothing() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> profileService.getUserProfile(USER_ID));
            verify(userRepository).findById(USER_ID);
            verifyNoInteractions(profileMapper);
        }
    }

    @Nested
    @DisplayName("updateUsername")
    class UpdateUsername {
        private static final String NEW_USERNAME = "testUser123";

        private UsernameChangePayload requestPayload;

        @BeforeEach
        void init() {
            requestPayload = new UsernameChangePayload(NEW_USERNAME);
        }

        @Test
        @DisplayName("updates username, and returns user profile")
        void updateUsername_userExists_updatesUsernameAndReturnsProfileInfo() {
            ProfileResponsePayload expectedResponse = new ProfileResponsePayload(
                    NEW_USERNAME,
                    EMAIL,
                    localUser.getCreatedAt()
            );

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(localUser));
            when(userRepository.save(localUser)).thenReturn(localUser);
            when(profileMapper.toDto(localUser)).thenReturn(expectedResponse);

            ProfileResponsePayload responsePayload = profileService.updateUsername(USER_ID, requestPayload);

            assertEquals(NEW_USERNAME, localUser.getUsername());
            assertEquals(NEW_USERNAME, responsePayload.username());
            assertEquals(EMAIL, responsePayload.email());
            verify(userRepository).save(localUser);
        }

        @Test
        @DisplayName("throws UserNotFoundException when the user doesn't exist")
        void updateUsername_userDoesNotExist_throwsUserNotFoundException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> profileService.updateUsername(USER_ID, requestPayload));

            verify(userRepository, never()).save(any());
            verifyNoInteractions(profileMapper);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        private static final String NEW_PASSWORD = "test@1227";
        private static final String NEW_ENCODED_PASSWORD = "$2a$10$encodedHash";

        private PasswordChangePayload requestPayload;

        @BeforeEach
        void init() {
            requestPayload = new PasswordChangePayload(OLD_PASSWORD, NEW_PASSWORD);
        }

        @Test
        @DisplayName("updates the password and publishes a confirmation event when the current password matches")
        void changePassword_currentPasswordMatches_updatesPasswordAndSendsEmail() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(localUser));
            when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD)).thenReturn(true);
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_ENCODED_PASSWORD);

            profileService.changePassword(USER_ID, requestPayload);

            assertEquals(NEW_ENCODED_PASSWORD, localUser.getPassword());
            verify(userRepository).save(localUser);
            verify(mailEventPublisher).publish(eq(EMAIL), eq("Your Password Has Been Changed"), anyString());
        }

        @Test
        @DisplayName("throws UserNotFoundException when the user doesn't exist")
        void changePassword_userDoesNotExist_throwsUserNotFoundException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> profileService.changePassword(USER_ID, requestPayload));

            verifyNoInteractions(passwordEncoder, mailEventPublisher);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when the current password doesn't match")
        void changePassword_currentPasswordDoesNotMatch_throwsInvalidCredentialsException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(localUser));
            when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD)).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> profileService.changePassword(USER_ID, requestPayload));

            verify(userRepository, never()).save(any());
            verifyNoInteractions(mailEventPublisher);
        }
    }
}