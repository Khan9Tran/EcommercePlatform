package com.hkteam.ecommerce_platform.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hkteam.ecommerce_platform.dto.request.EmailMessageRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailSender {

    JavaMailSender emailSender;
    TemplateEngine templateEngine;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void sendEmail(EmailMessageRequest request) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());

        Map<String, Object> variables = new HashMap<>();
        variables.put("tokenUrl", request.getTokenUrl());

        String htmlContent = renderTemplate(request.getTemplate(), variables);

        helper.setText(htmlContent, true);

        try {
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending email", e);
        }

        log.info("Email sent to {}", request.getTo());
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
