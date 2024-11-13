package com.hkteam.ecommerce_platform.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ExchangeTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.AuthenticationResponse;
import com.hkteam.ecommerce_platform.entity.user.ExternalAuth;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.EmailValidationStatus;
import com.hkteam.ecommerce_platform.enums.Gender;
import com.hkteam.ecommerce_platform.enums.Provider;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ExternalAuthRepository;
import com.hkteam.ecommerce_platform.repository.RoleRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import com.hkteam.ecommerce_platform.repository.httpclient.OutboundFacebookUserClient;
import com.hkteam.ecommerce_platform.repository.httpclient.OutboundIdentityClient;
import com.hkteam.ecommerce_platform.repository.httpclient.OutboundIdentityFacebookClient;
import com.hkteam.ecommerce_platform.repository.httpclient.OutboundUserClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExternalAuthService {

    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    OutboundFacebookUserClient outboundFacebookUserClient;
    OutboundIdentityFacebookClient outboundIdentityFacebookClient;
    UserRepository userRepository;
    ExternalAuthRepository externalAuthRepository;
    RoleRepository roleRepository;
    AuthenticationService authenticationService;

    @NonFinal
    @Value("${outbound.google.client-id}")
    String CLIENT_ID;

    @NonFinal
    @Value("${outbound.facebook.client-id}")
    String FACEBOOK_CLIENT_ID;

    @Value("${outbound.google.client-secret}")
    @NonFinal
    String CLIENT_SECRET;

    @Value("${outbound.facebook.client-secret}")
    @NonFinal
    String FACEBOOK_CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.google.redirect-uri}")
    String REDIRECT_URI;

    @NonFinal
    @Value("${outbound.facebook.redirect-uri}")
    String FACEBOOK_REDIRECT_URI;

    @NonFinal
    String GRANT_TYPE = "authorization_code";

    public AuthenticationResponse googleAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        // onboard user if not already onboarded
        var externalAuths = externalAuthRepository.findByProviderAndProviderID(Provider.GOOGLE, userInfo.getId());
        if (externalAuths.isEmpty()) {

            if (userRepository.findByEmail(userInfo.getEmail()).isPresent()) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }

            var roles = roleRepository.findByName(RoleName.USER);
            if (roles.isEmpty()) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }

            String email = userInfo.getEmail();
            int atIndex = email.indexOf("@");
            String baseUsername = "G_" + (atIndex != -1 ? email.substring(0, atIndex) : email);

            int counter = 1;
            String username = baseUsername;

            while (userRepository.findByUsername(username).isPresent()) {
                username = baseUsername + counter;
                counter++;
            }

            ExternalAuth externalAuth = ExternalAuth.builder()
                    .provider(Provider.GOOGLE)
                    .providerID(userInfo.getId())
                    .user(User.builder()
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .emailValidationStatus(EmailValidationStatus.VERIFIED.name())
                            .emailTokenGeneratedAt(Instant.now())
                            .imageUrl(userInfo.getPicture())
                            .roles(new HashSet<>(List.of(roles.get())))
                            .gender(Gender.OTHER)
                            .username(username)
                            .build())
                    .build();

            try {
                externalAuthRepository.save(externalAuth);
            } catch (Exception e) {
                log.info("Error: {}", e.getMessage());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
        }

        var user = userRepository
                .findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.isBlocked()) throw new AppException(ErrorCode.USER_HAS_BEEN_BLOCKED);

        var token = authenticationService.generateToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public AuthenticationResponse facebookAuthenticate(String code) {
        log.info("code: {}", code);
        try {
            var response = outboundIdentityFacebookClient.exchangeToken(
                    FACEBOOK_CLIENT_ID, FACEBOOK_REDIRECT_URI, FACEBOOK_CLIENT_SECRET, code);

            var userInfo =
                    outboundFacebookUserClient.getUserInfo(response.getAccessToken(), "id, name, email, picture");
            var externalAuths = externalAuthRepository.findByProviderAndProviderID(Provider.FACEBOOK, userInfo.getId());
            if (externalAuths.isEmpty()) {

                if (userRepository.findByEmail(userInfo.getEmail()).isPresent()) {
                    throw new AppException(ErrorCode.EMAIL_EXISTED);
                }

                var roles = roleRepository.findByName(RoleName.USER);
                if (roles.isEmpty()) {
                    throw new AppException(ErrorCode.ROLE_NOT_FOUND);
                }

                String email = userInfo.getEmail();
                int atIndex = email.indexOf("@");
                String baseUsername = "F_" + (atIndex != -1 ? email.substring(0, atIndex) : email);

                int counter = 1;
                String username = baseUsername;

                while (userRepository.findByUsername(username).isPresent()) {
                    username = baseUsername + counter;
                    counter++;
                }

                ExternalAuth externalAuth = ExternalAuth.builder()
                        .provider(Provider.GOOGLE)
                        .providerID(userInfo.getId())
                        .user(User.builder()
                                .email(userInfo.getEmail())
                                .name(userInfo.getName())
                                .emailValidationStatus(EmailValidationStatus.VERIFIED.name())
                                .emailTokenGeneratedAt(Instant.now())
                                .imageUrl(userInfo.getPicture().getData().getUrl())
                                .roles(new HashSet<>(List.of(roles.get())))
                                .gender(Gender.OTHER)
                                .username(username)
                                .build())
                        .build();

                try {
                    externalAuthRepository.save(externalAuth);
                } catch (Exception e) {
                    log.info("Error: {}", e.getMessage());
                    throw new AppException(ErrorCode.UNKNOWN_ERROR);
                }
            }

            var user = userRepository
                    .findByEmail(userInfo.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (user.isBlocked()) throw new AppException(ErrorCode.USER_HAS_BEEN_BLOCKED);

            var token = authenticationService.generateToken(user);
            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();
        } catch (Exception e) {
            log.info("Error: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }
}
