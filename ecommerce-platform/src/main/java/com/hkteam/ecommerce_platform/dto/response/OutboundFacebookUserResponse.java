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
@Getter
@Setter
public class OutboundFacebookUserResponse {
    String id;
    String email;
    String name;
    Picture picture;

    public static class Picture {
        @Getter
        @Setter
        private Data data;
    }

    public static class Data {
        @Getter
        @Setter
        private String url;
    }
}
