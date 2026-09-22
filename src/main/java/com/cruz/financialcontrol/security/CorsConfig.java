package com.cruz.financialcontrol.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Class Name: CorsConfig
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */
@Configuration
public class CorsConfig {

    @Value("${security.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${security.cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${security.cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${security.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${security.cors.max-age}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders);
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
