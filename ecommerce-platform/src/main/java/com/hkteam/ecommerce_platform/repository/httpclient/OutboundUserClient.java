package com.hkteam.ecommerce_platform.repository.httpclient;

import com.hkteam.ecommerce_platform.dto.request.ExchangeTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.ExchangeTokenResponse;
import com.hkteam.ecommerce_platform.dto.response.OutboundUserResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {
    @GetMapping(value = "/oauth/v1/userinfo")
    OutboundUserResponse getUserInfo(@RequestParam("alt") String alt,
                                     @RequestParam("access_token") String accessToken);
}
