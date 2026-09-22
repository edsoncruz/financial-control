package com.cruz.financialcontrol.service.impl;

import com.cruz.financialcontrol.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Class Name: LoggingEmailService
 * Description:
 *
 * @author edson
 * @date 21/09/2026
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendWelcomeEmail(String toEmail, String userName) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom("welcome@financialcontrol.com");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Financial Control");
            helper.setText("Now that you have created your account, you can start managing your finances effectively.", false);

            mailSender.send(message);

            log.info("Welcome email sent via SMTP to {}.", toEmail);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send welcome email to %s ".formatted(toEmail), ex);
        }

    }
}