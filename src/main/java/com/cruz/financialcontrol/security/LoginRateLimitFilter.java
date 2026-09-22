package com.cruz.financialcontrol.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles requests to the public authentication endpoints (login/register) per client IP,
 * to slow down brute-force / credential-stuffing attempts. This complements (but doesn't replace)
 * the per-account lockout in UserService, since it protects against attackers spraying many
 * different email addresses from the same IP.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int CAPACITY = 5;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final JsonMapper jsonMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)  throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isGuardedPath(path)) {
            String clientIp = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> Bucket.builder()
                    .addLimit(Bandwidth.builder()
                            .capacity(CAPACITY)
                            .refillGreedy(CAPACITY, REFILL_PERIOD)
                            .build())
                    .build());

            if (!bucket.tryConsume(1)) {
                response.setStatus(getTooManyRequestsProblemDetail().getStatus());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(jsonMapper.writeValueAsString(getTooManyRequestsProblemDetail()));

                log.warn("Rate limit exceeded for IP {} on {}", clientIp, path);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGuardedPath(String path) {
        return path.endsWith("/auth/login") || path.endsWith("/auth/register");
    }

    private ProblemDetail getTooManyRequestsProblemDetail() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Please try again later");
        problem.setTitle(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());

        return problem;
    }
}
