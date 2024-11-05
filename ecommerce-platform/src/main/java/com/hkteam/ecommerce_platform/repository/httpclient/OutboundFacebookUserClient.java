package com.hkteam.ecommerce_platform.repository.httpclient;

import com.hkteam.ecommerce_platform.dto.response.OutboundFacebookUserResponse;
import com.hkteam.ecommerce_platform.dto.response.OutboundUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outbound-facebook-user-client", url = "https://graph.facebook.com")
public interface OutboundFacebookUserClient {
    @GetMapping(value = "/me")
    OutboundFacebookUserResponse getUserInfo(@RequestParam("access_token") String accessToken,
                                                          @RequestParam(value = "fields") String fields
    );
}
