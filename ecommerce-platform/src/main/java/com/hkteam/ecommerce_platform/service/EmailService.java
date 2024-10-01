package com.hkteam.ecommerce_platform.service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hkteam.ecommerce_platform.dto.request.EmailRequest;
import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.enums.EmailValidationStatus;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.EmailMapper;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import com.hkteam.ecommerce_platform.util.JwtUtils;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {

    JavaMailSender emailSender;
    TemplateEngine templateEngine;

    UserRepository userRepository;
    EmailMapper emailMapper;

    @NonFinal
    Authentication authentication;

    JwtUtils jwtUtils;

    public EmailResponse updateEmail(EmailRequest request) {
        authentication = SecurityContextHolder.getContext().getAuthentication();

        String newEmail = request.getEmail();
        var user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getId().equals(request.getUserId())) throw new AppException(ErrorCode.UNAUTHORIZED);

        if (user.getEmail() != null && user.getEmail().equals(newEmail))
            throw new AppException(ErrorCode.NEW_EMAIL_SAME_CURRENT_EMAIL);

        user.setEmail(newEmail);
        user.setEmailValidationStatus(EmailValidationStatus.UNVERIFIED.name());
        user.setEmailTokenGeneratedAt(null);
        user.setEmailValidationToken(null);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        return emailMapper.toEmailResponse(user);
    }

    public void sendMailValidation() throws MessagingException, JOSEException {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getEmailTokenGeneratedAt() != null
                && user.getEmailTokenGeneratedAt().isAfter(Instant.now().minus(Duration.ofSeconds(30))))
            throw new AppException(ErrorCode.EMAIL_TOKEN_TOO_RECENT);

        String jti = UUID.randomUUID().toString();
        String token = jwtUtils.generateToken(user.getEmail(), jti);

        user.setEmailValidationStatus(EmailValidationStatus.PENDING.name());
        user.setEmailValidationToken(jti);
        user.setEmailTokenGeneratedAt(Instant.now());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String tokenUrl = "http://localhost:8080/emails/verify?token=" + token;

        setupMailValidation(user.getEmail(), "Xác thực email", tokenUrl, "email-validation");
    }

    @Async
    private void setupMailValidation(String to, String subject, String tokenUrl, String template)
            throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);

        Map<String, Object> variables = new HashMap<>();
        variables.put("tokenUrl", tokenUrl);

        String htmlContent = renderTemplate("email-validation", variables);

        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    public void verifyEmail(String token) throws ParseException, JOSEException {
        var claims = jwtUtils.decodeToken(token);
        String jti = claims.getJWTID();

        var user = userRepository
                .findByEmailValidationToken(jti)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        if (user.getEmailValidationStatus().equals(EmailValidationStatus.VERIFIED.name()))
            throw new AppException(ErrorCode.ALREADY_VERIFIED);

        if (!user.getEmail().equals(claims.getSubject())) throw new AppException(ErrorCode.TOKEN_INVALID);

        try {
            user.setEmailValidationStatus(EmailValidationStatus.VERIFIED.name());
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.VALIDATION_EMAIL_FAILURE);
        }
    }
}
