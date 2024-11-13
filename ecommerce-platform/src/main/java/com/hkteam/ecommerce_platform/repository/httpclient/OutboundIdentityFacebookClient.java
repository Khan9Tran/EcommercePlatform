package com.hkteam.ecommerce_platform.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hkteam.ecommerce_platform.dto.response.ExchangeFacebookTokenResponse;

@FeignClient(name = "facebook-outbound-identity", url = "https://graph.facebook.com")
public interface OutboundIdentityFacebookClient {

    @GetMapping("/v21.0/oauth/access_token")
    ExchangeFacebookTokenResponse exchangeToken(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code);
}
