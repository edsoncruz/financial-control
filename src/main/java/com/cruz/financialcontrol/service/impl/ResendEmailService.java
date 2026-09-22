package com.cruz.financialcontrol.service.impl;

import com.cruz.financialcontrol.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Class Name: ResendEmailService
 * Description: Sends transactional emails through the Resend HTTP API
 * (<a href="https://resend.com/docs/api-reference/emails/send-email">...</a>).
 *
 * @author edson
 * @date 21/09/2026
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
public class ResendEmailService implements EmailService {

    private final RestClient client;

    public ResendEmailService(@Value("${resend.api.token}") String resendApiToken) {
        this.client = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + resendApiToken)
                .build();
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName) {

        Map<String, Object> payload = Map.of(
                "from", "welcome@financialcontrol.com",
                "to", List.of(toEmail),
                "subject", "Welcome to Financial Control",
                "html", "<p>Hi " + userName + ", welcome aboard!</p>"
        );

        client.post()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        log.info("Welcome email sent via Resend to {}", toEmail);
    }
}
