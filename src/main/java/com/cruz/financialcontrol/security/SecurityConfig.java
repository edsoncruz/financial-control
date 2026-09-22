package com.cruz.financialcontrol.security;

import com.cruz.financialcontrol.exception.NotFoundException;
import com.cruz.financialcontrol.exception.handler.RestAuthenticationEntryPoint;
import com.cruz.financialcontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthFilter jwtAuthFilter,
            LoginRateLimitFilter loginRateLimitFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            CorsConfigurationSource corsConfigurationSource) throws Exception {

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)             // REST API — no CSRF needed
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register").permitAll() // login/register are public
                        .requestMatchers("/auth/login").permitAll()  // login/login are public
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()                // everything else requires token
                )
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no sessions — JWT handles state
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginRateLimitFilter, JwtAuthFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder argon2PasswordEncoder() {
        final int SALT_LENGTH = 16;
        final int HASH_LENGTH = 32;
        final int PARALLELISM = 2;
        final int MEMORY_COST = 1 << 15; //32 MiB
        final int ITERATIONS = 2;

        return new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY_COST, ITERATIONS);
    }
}