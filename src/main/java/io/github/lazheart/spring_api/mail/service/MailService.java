package io.github.lazheart.spring_api.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String to, String username, String verificationCode) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationCode", verificationCode);
        
        String process = templateEngine.process("verificationcode", context);
        sendHtmlEmail(to, "Verifica tu cuenta - Seguridad", process);
    }

    @Async
    public void sendVerificationSuccessEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("appUrl", "http://localhost:8080");
        
        String process = templateEngine.process("verificationsuccessful", context);
        sendHtmlEmail(to, "Cuenta verificada exitosamente", process);
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("appUrl", "http://localhost:8080");
        
        String process = templateEngine.process("welcomeuser", context);
        sendHtmlEmail(to, "Bienvenido a la plataforma", process);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }
}
