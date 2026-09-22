package com.cruz.financialcontrol.service;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.model.dto.user.*;
import com.cruz.financialcontrol.model.entity.User;
import com.cruz.financialcontrol.model.mapper.UserMapper;
import com.cruz.financialcontrol.repository.UserRepository;
import com.cruz.financialcontrol.security.JwtService;
import com.cruz.financialcontrol.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityUtils securityUtils;

    private UserService userService;

    private static final Long CURRENT_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, jwtService, authenticationManager, passwordEncoder, securityUtils);
    }

    @Test
    void create_shouldEncodePasswordAndPersistUser_whenEmailIsAvailable() {
        CreateUserDTO dto = new CreateUserDTO("John Doe", "john@example.com", "plainPassword");

        User mapped = new User();
        mapped.setName("John Doe");
        mapped.setEmail("john@example.com");
        mapped.setPassword("plainPassword");

        when(userMapper.toEntity(dto)).thenReturn(mapped);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(mapped)).thenReturn(mapped);
        when(userMapper.toResponseDTO(mapped)).thenReturn(new UserResponseDTO(1L, "John Doe", "john@example.com"));

        UserResponseDTO response = userService.create(dto);

        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(mapped.getPassword()).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void create_shouldThrowBusinessRuleException_whenEmailAlreadyExists() {
        CreateUserDTO dto = new CreateUserDTO("John Doe", "john@example.com", "plainPassword");

        User mapped = new User();
        mapped.setEmail("john@example.com");
        User existing = new User();
        existing.setEmail("john@example.com");

        when(userMapper.toEntity(dto)).thenReturn(mapped);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldChangeNameAndEmail_whenNewEmailIsAvailable() {
        User existing = new User();
        existing.setId(CURRENT_USER_ID);
        existing.setName("Old Name");
        existing.setEmail("old@example.com");

        UpdateUserDTO dto = new UpdateUserDTO("New Name", "new@example.com");

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponseDTO(existing)).thenReturn(new UserResponseDTO(1L, "New Name", "new@example.com"));

        UserResponseDTO response = userService.update(dto);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_shouldNotCheckEmailUniqueness_whenEmailIsUnchanged() {
        User existing = new User();
        existing.setId(CURRENT_USER_ID);
        existing.setName("Old Name");
        existing.setEmail("same@example.com");

        UpdateUserDTO dto = new UpdateUserDTO("New Name", "same@example.com");

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponseDTO(existing)).thenReturn(new UserResponseDTO(1L, "New Name", "same@example.com"));

        userService.update(dto);

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void update_shouldThrowBusinessRuleException_whenNewEmailAlreadyTaken() {
        User existing = new User();
        existing.setId(CURRENT_USER_ID);
        existing.setEmail("old@example.com");

        UpdateUserDTO dto = new UpdateUserDTO("New Name", "taken@example.com");

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowNotFoundException_whenCurrentUserDoesNotExist() {
        UpdateUserDTO dto = new UpdateUserDTO("New Name", "new@example.com");

        when(securityUtils.getCurrentUserId()).thenReturn(CURRENT_USER_ID);
        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void login_shouldReturnJwtToken_whenCredentialsAreValid() {
        AuthLoginDTO request = new AuthLoginDTO("john@example.com", "plainPassword");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john@example.com").password("encoded").authorities("USER").build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponseDTO response = userService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_shouldPropagateAuthenticationException_whenCredentialsAreInvalid() {
        AuthLoginDTO request = new AuthLoginDTO("john@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> userService.login(request)).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService);
    }
}
