package com.hkteam.ecommerce_platform.dto.response;

import com.hkteam.ecommerce_platform.enums.RoleName;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
}
