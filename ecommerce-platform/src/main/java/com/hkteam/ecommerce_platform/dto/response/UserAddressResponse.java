package com.hkteam.ecommerce_platform.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserAddressResponse {
    //mapper
    Long id;
    String recipientName;
    String phone;


    String firstLine;
    String secondLine;

    @Builder.Default
    Boolean isDefault = false;

    @Builder.Default
    Boolean isStoreAddress = false;
}
