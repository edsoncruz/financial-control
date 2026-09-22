package com.cruz.financialcontrol.service.impl;

import com.cruz.financialcontrol.service.EmailService;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mailtrap")
public class MailTrapEmailService implements EmailService {

    private final MailtrapClient client;

    public MailTrapEmailService(@Value("${mailtrap.api.token}") String mailtrapToken,
                                 @Value("${mailtrap.api.inboxid}") long inboxId) {

        final MailtrapConfig config = new MailtrapConfig.Builder()
                .token(mailtrapToken)
                .sandbox(true)
                .inboxId(inboxId)
                .build();

        this.client = MailtrapClientFactory.createMailtrapClient(config);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName) {
        final MailtrapMail mail = MailtrapMail.builder()
                .from(new Address("welcome@financialcontrol.com", "Financial Control"))
                .to(List.of(new Address(toEmail)))
                .subject("Welcome to Financial Control, %s!".formatted(userName))
                .text("Now that you have created your account, you can start managing your finances effectively.")
                .category("Integration Test")
                .build();

        client.send(mail);

        log.info("Welcome email sent via MailTrap to {}.", toEmail);
    }
}
