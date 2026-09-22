package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.messaging.OutboxEventPublisher;
import com.cruz.financialcontrol.model.dto.user.*;
import com.cruz.financialcontrol.model.entity.User;
import com.cruz.financialcontrol.model.mapper.UserMapper;
import com.cruz.financialcontrol.repository.UserRepository;
import com.cruz.financialcontrol.security.JwtService;
import com.cruz.financialcontrol.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 2;
    private static final Duration ACCOUNT_LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final OutboxEventPublisher outboxEventPublisher;

    /**
     * Creates a new user in the system.
     * @param createUserDTO The DTO containing the user information to be created.
     * @return UserResponseDTO containing the created user's information.
     * @throws BusinessRuleException if the email already exists in the system.
     */
    @Transactional
    public UserResponseDTO create(CreateUserDTO createUserDTO) {
        User user = userMapper.toEntity(createUserDTO);

        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new BusinessRuleException("Email already exists: " + existingUser.getEmail());
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = userRepository.save(user);

        outboxEventPublisher.publishUserCreated(user);

        log.info("User created with ID {}", user.getId());

        return userMapper.toResponseDTO(user);
    }

    /**
     * Updates the current user's information in the system.
     * @param updateUserDTO The DTO containing the updated user information.
     * @return UserResponseDTO containing the updated user's information.
     * @throws NotFoundException if the user is not found in the system.
     * @throws BusinessRuleException if the new email already exists in the system.
     */
    public UserResponseDTO update(final UpdateUserDTO updateUserDTO) {
        User user = userRepository
                .findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEmail().equals(updateUserDTO.email()) && userRepository.existsByEmail(updateUserDTO.email())) {
            throw new BusinessRuleException("Email already exists: " + updateUserDTO.email());
        }

        user.setName(updateUserDTO.name());
        user.setEmail(updateUserDTO.email());

        user = userRepository.save(user);

        log.info("User updated with ID {}", user.getId());

        return userMapper.toResponseDTO(user);
    }

    /**
     * Deletes the currently logged-in user from the system.
     * @throws NotFoundException if the user is not found in the system.
     */
    public void deleteLoggedUser() {
        User user = userRepository
                .findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        userRepository.delete(user);

        log.info("User deleted with ID {}", securityUtils.getCurrentUserId());
    }

    /**
     * Finds the currently logged-in user in the system.
     * @return UserResponseDTO containing the logged-in user's information.
     * @throws NotFoundException if the user is not found in the system.
     */
    public UserResponseDTO findLoggedUser() {
        User user = userRepository
                .findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userMapper.toResponseDTO(user);
    }

    /**
     * Authenticates a user and generates a JWT token.
     * Tracks failed attempts per account and temporarily locks it out after
     * {@value #MAX_FAILED_LOGIN_ATTEMPTS} consecutive failures, to blunt brute-force attacks
     * that persist across IPs (complements the IP-based {@code LoginRateLimitFilter}).
     * @param request The DTO containing the user's login credentials.
     * @return AuthResponseDTO containing the generated JWT token.
     * @throws AuthenticationException if the credentials are invalid.
     * @throws BusinessRuleException if the account is currently locked out.
     */
    public AuthResponseDTO login(AuthLoginDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User with email %s not found".formatted(request.email())));

        if (user.getLockedUntil() != null &&  Instant.now().isBefore(user.getLockedUntil())) {
            throw new BusinessRuleException("Account temporarily locked due to repeated failed login attempts. Please try again later.", HttpStatus.LOCKED);
        }

        try {
            UsernamePasswordAuthenticationToken authData = new UsernamePasswordAuthenticationToken(request.email(), request.password());

            // Validates credentials — throws AuthenticationException if invalid
            Authentication auth = authenticationManager.authenticate(authData);

            if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            Objects.requireNonNull(userDetails, "UserDetails cannot be null");

            String token = jwtService.generateToken(userDetails);

            log.info("User logged in with email {}", request.email());

            return new AuthResponseDTO(token);
        } catch (BadCredentialsException ex) {

            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(Instant.now().plus(ACCOUNT_LOCKOUT_DURATION));
                log.warn("Account locked for {} after {} failed login attempts: {}", ACCOUNT_LOCKOUT_DURATION, user.getFailedLoginAttempts(), request.email());
            }

            userRepository.save(user);
            throw ex;
        }
    }

    /**
     * Changes the password of the currently logged-in user.
     * @param changePasswordDTO The DTO containing the current and new passwords.
     */
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        User user = userRepository
                .findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if(!passwordEncoder.matches(changePasswordDTO.currentPassword(), user.getPassword())) {
            throw new BusinessRuleException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.newPassword()));

        userRepository.save(user);

        log.info("User changed password with ID {}", user.getId());
    }
}
