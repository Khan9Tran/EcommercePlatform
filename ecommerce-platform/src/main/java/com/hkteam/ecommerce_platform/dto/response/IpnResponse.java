package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpnResponse {
    String RspCode;
    String Message;
}

