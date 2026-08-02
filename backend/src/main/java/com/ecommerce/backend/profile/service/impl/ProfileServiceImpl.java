package com.ecommerce.backend.profile.service.impl;

import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.auth.exception.InvalidCredentialsException;
import com.ecommerce.backend.auth.exception.UserNotFoundException;
import com.ecommerce.backend.auth.repository.UserRepository;
import com.ecommerce.backend.integration.mail.publisher.MailEventPublisher;
import com.ecommerce.backend.profile.dto.request.PasswordChangePayload;
import com.ecommerce.backend.profile.dto.request.UsernameChangePayload;
import com.ecommerce.backend.profile.dto.response.ProfileResponsePayload;
import com.ecommerce.backend.profile.mapper.ProfileMapper;
import com.ecommerce.backend.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailEventPublisher mailEventPublisher;

    @Override
    public ProfileResponsePayload getUserProfile(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        return profileMapper.toDto(user);
    }

    @Override
    public ProfileResponsePayload updateUsername(UUID userId, UsernameChangePayload requestPayload) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setUsername(requestPayload.newUsername());
        User savedUser = userRepository.save(user);

        return profileMapper.toDto(savedUser);
    }

    @Override
    public void changePassword(UUID userId, PasswordChangePayload requestPayload) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(requestPayload.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(requestPayload.newPassword()));
        userRepository.save(user);

        String subject = "Your Password Has Been Changed";

        String message = """
                        Hello,

                        Your account password was successfully changed.

                        If you made this change, no further action is required.

                        If you do not recognize this activity, please secure your account immediately.

                        Regards
                        """;

        mailEventPublisher.publish(user.getEmail(), subject, message);
    }
}
