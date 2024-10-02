package com.hkteam.ecommerce_platform.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String username;
    String id;
    String name;
    List<RoleResponse> roles;
    List<ExternalAuthResponse> externalAuth;
}
