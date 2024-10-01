package com.hkteam.ecommerce_platform.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import com.hkteam.ecommerce_platform.dto.request.ExchangeTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.ExchangeTokenResponse;

import feign.QueryMap;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
