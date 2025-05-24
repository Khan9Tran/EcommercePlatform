package com.hkteam.ecommerce_platform.service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.constant.TokenPurpose;
import com.hkteam.ecommerce_platform.dto.request.*;
import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.dto.response.PhoneResponse;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.EmailValidationStatus;
import com.hkteam.ecommerce_platform.enums.PhoneValidationStatus;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.EmailMapper;
import com.hkteam.ecommerce_platform.mapper.PhoneMapper;
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
@Slf4j
public class EmailService {
    @Value("${cors.frontend-url}")
    String frontendUrl;

    @Value("${cors.backend-url}")
    String backendUrl;
    RabbitTemplate rabbitTemplate;

    UserRepository userRepository;
    EmailMapper emailMapper;
    PhoneMapper phoneMapper;

    UserService userService;

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

    public PhoneResponse updatePhone(PhoneRequest request) {

        String newPhone = request.getPhone();
        var user = authenticationUtil.getAuthenticatedUser();

        if (!user.getId().equals(request.getUserId())) throw new AppException(ErrorCode.UNAUTHORIZED);

        if (user.getPhone() != null && user.getPhone().equals(newPhone))
            throw new AppException(ErrorCode.NEW_PHONE_SAME_CURRENT_PHONE);

        user.setPhone(newPhone);
        user.setPhoneValidationStatus(PhoneValidationStatus.UNVERIFIED.name());
        user.setPhoneTokenGeneratedAt(null);
        user.setPhoneValidationToken(null);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        return phoneMapper.toPhoneResponse(user);
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
        String token = jwtUtils.generateToken(user.getEmail(), jti, TokenPurpose.EMAIL);

        user.setEmailValidationStatus(EmailValidationStatus.PENDING.name());
        user.setEmailValidationToken(jti);
        user.setEmailTokenGeneratedAt(Instant.now());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String tokenUrl = frontendUrl + "/verify?token=" + token;
        try {
            sendMailValidation(user.getEmail(), "Xác thực email", tokenUrl, "email-validation");
        } catch (Exception e) {
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

    public void verifyEmail(VerifyEmailRequest request) throws ParseException, JOSEException {
        var user = toUser(request.getToken(), TokenPurpose.EMAIL);

        if (user.getEmailValidationStatus().equals(EmailValidationStatus.VERIFIED.name()))
            throw new AppException(ErrorCode.ALREADY_VERIFIED);

        try {
            user.setEmailValidationStatus(EmailValidationStatus.VERIFIED.name());
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.VALIDATION_EMAIL_FAILURE);
        }
    }

    public void sendPasswordResetEmail(ResetPasswordRequest request) throws JOSEException {
        var user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getEmailValidationStatus() == null
                || !user.getEmailValidationStatus().equals(EmailValidationStatus.VERIFIED.name())) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        String jti = UUID.randomUUID().toString();
        String token = jwtUtils.generateToken(user.getEmail(), jti, TokenPurpose.RESET_PASSWORD);

        user.setEmailValidationToken(jti);
        user.setEmailTokenGeneratedAt(Instant.now());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String tokenUrl = backendUrl +"/emails/reset-password?token=" + token;
        try {
            sendMailValidation(user.getEmail(), "Reset password", tokenUrl, "reset-password");
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_SEND_FAILURE);
        }
    }

    public void confirmResetPassword(ConfirmResetPassordRequest request) throws ParseException, JOSEException {
        var user = toUser(request.getToken(), TokenPurpose.RESET_PASSWORD);

        if (user.getEmailValidationStatus() == null
                || !user.getEmailValidationStatus().equals(EmailValidationStatus.VERIFIED.name())) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (!request.getPasswordConfirmation().equals(request.getPassword()))
            throw new AppException(ErrorCode.PASSWORDS_DO_NOT_MATCH);

        user.setEmailValidationToken(null);
        userService.setPassword(user, request.getPassword());
    }

    private User toUser(String token, String purpose) throws ParseException, JOSEException {
        var claims = jwtUtils.decodeToken(token);
        String jti = claims.getJWTID();

        String tokenPurpose = claims.getStringClaim("purpose");
        if (!purpose.equals(tokenPurpose)) throw new AppException(ErrorCode.TOKEN_INVALID);

        var user = userRepository
                .findByEmailValidationToken(jti)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_INVALID));

        if (!user.getEmail().equals(claims.getSubject())) throw new AppException(ErrorCode.TOKEN_INVALID);

        return user;
    }
}
