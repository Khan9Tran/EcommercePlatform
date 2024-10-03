package com.hkteam.ecommerce_platform.service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.EmailMessageRequest;
import com.hkteam.ecommerce_platform.dto.request.EmailRequest;
import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.enums.EmailValidationStatus;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.EmailMapper;
import com.hkteam.ecommerce_platform.rabbitmq.RabbitMQConfig;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.JwtUtils;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    RabbitTemplate rabbitTemplate;

    UserRepository userRepository;
    EmailMapper emailMapper;

    AuthenticatedUserUtil authenticationUtil;

    JwtUtils jwtUtils;

    public EmailResponse updateEmail(EmailRequest request) {

        String newEmail = request.getEmail();
        var user = authenticationUtil.getAuthenticatedUser();

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

    public void sendMailValidation() throws JOSEException {
        var user = authenticationUtil.getAuthenticatedUser();

        if (user.getEmailValidationStatus() != null
                && user.getEmailValidationStatus().equals(EmailValidationStatus.VERIFIED.name()))
            throw new AppException(ErrorCode.ALREADY_VERIFIED);

        if (user.getEmailTokenGeneratedAt() != null
                && user.getEmailTokenGeneratedAt().isAfter(Instant.now().minus(Duration.ofSeconds(30))))
            throw new AppException(ErrorCode.EMAIL_TOKEN_TOO_RECENT);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new AppException(ErrorCode.EMAIL_NOT_BLANK);
        }

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
        try {
            sendMailValidation(user.getEmail(), "Xác thực email", tokenUrl, "email-validation");
        } catch (Exception e) {
            log.info("Error sending email {}", e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SEND_FAILURE);
        }
    }

    private void sendMailValidation(String to, String subject, String tokenUrl, String template) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_QUEUE,
                EmailMessageRequest.builder()
                        .to(to)
                        .subject(subject)
                        .tokenUrl(tokenUrl)
                        .template(template)
                        .build());

        log.info("Email sent to the queue for processing: {}", to);
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
