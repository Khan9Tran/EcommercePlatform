package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.request.ExchangeTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.AuthenticationResponse;
import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.entity.user.ExternalAuth;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.EmailValidationStatus;
import com.hkteam.ecommerce_platform.enums.Provider;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ExternalAuthRepository;
import com.hkteam.ecommerce_platform.repository.RoleRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import com.hkteam.ecommerce_platform.repository.httpclient.OutboundIdentityClient;
import com.hkteam.ecommerce_platform.repository.httpclient.OutboundUserClient;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExternalAuthService {

    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    UserRepository userRepository;
    ExternalAuthRepository externalAuthRepository;
    RoleRepository roleRepository;

    @NonFinal
    @Value("${outbound.google.client-id}")
    protected  String CLIENT_ID = "114329022200-hkajl8098uino3u4hjl1e30gmph9m80l.apps.googleusercontent.com";

    @Value("${outbound.google.client-secret}")
    @NonFinal
    protected  String CLIENT_SECRET = "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";

    @Value("${outbound.google.redirect-uri}")
    @NonFinal
    protected String REDIRECT_URI = "http://localhost:3000/authenticate";

    @NonFinal
    protected String GRANT_TYPE = "authorization_code";

    public AuthenticationResponse googleAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        response.getAccessToken();

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        if (userRepository.findByEmail(userInfo.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        var roles = roleRepository.findByName(RoleName.USER);
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        long currentTimeMillis = System.currentTimeMillis();
        String username = "G_" + userInfo.getEmail().split("@")[0] + "_" + currentTimeMillis;

        ExternalAuth externalAuth = ExternalAuth.builder()
                .provider(Provider.GOOGLE).providerID(userInfo.getId())
                .user(User.builder()
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .emailValidationStatus(EmailValidationStatus.VERIFIED.name())
                        .imageUrl(userInfo.getPicture())
                        .roles(new HashSet<>(List.of(roles.get())))
                        .username(username)
                        .build())
                .build();

        try {
            externalAuthRepository.save(externalAuth);
        } catch (Exception e) {
            log.info("Error: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return AuthenticationResponse.builder().token(response.getAccessToken()).build();
    }

}
