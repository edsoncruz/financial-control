package com.cruz.financialcontrol.security;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import com.cruz.financialcontrol.model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_shouldReturnAuthenticatedUserId_whenPrincipalIsUserPrincipal() {
        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        user.setPassword("encoded");

        UserPrincipal principal = new UserPrincipal(user);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long currentUserId = securityUtils.getCurrentUserId();

        assertThat(currentUserId).isEqualTo(42L);
    }

    @Test
    void getCurrentUserId_shouldThrowBusinessRuleException_whenNoAuthenticationPresent() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(securityUtils::getCurrentUserId)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User not authenticated");
    }

    @Test
    void getCurrentUserId_shouldThrowBusinessRuleException_whenAuthenticationIsNotAuthenticated() {
        User user = new User();
        user.setId(1L);
        UserPrincipal principal = new UserPrincipal(user);

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(securityUtils::getCurrentUserId)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User not authenticated");
    }

    @Test
    void getCurrentUserId_shouldThrowBusinessRuleException_whenPrincipalIsNotUserPrincipal() {
        var authentication = new UsernamePasswordAuthenticationToken("someRandomPrincipal", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(securityUtils::getCurrentUserId)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("User not authenticated");
    }
}
