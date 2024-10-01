package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.request.ExchangeTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.AuthenticationResponse;
import com.hkteam.ecommerce_platform.repository.OutboundIdentityClient;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExternalAuthService {

    OutboundIdentityClient outboundIdentityClient;
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
        log.info("response: " + response);
        return AuthenticationResponse.builder().token(response.getAccessToken()).build();
    }
}
