package com.hkteam.ecommerce_platform.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoomResponse {
    String id;
    String storeId;
    String storeName;
    String userId;
    String userName;
    String storeImageUrl;
    String userImageUrl;
    String lastMessage;
    String lastTimeMessage;
    Timestamp createdAt;
    Timestamp updatedAt;
}
