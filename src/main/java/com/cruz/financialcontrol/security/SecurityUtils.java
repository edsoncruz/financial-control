package com.cruz.financialcontrol.security;

import com.cruz.financialcontrol.exception.BusinessRuleException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Returns the id of the currently authenticated user.
     * Throws if there is no authenticated {@link UserPrincipal} in the security context.
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessRuleException("User not authenticated");
        }

        return principal.getId();
    }
}
