package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;

import com.hkteam.ecommerce_platform.dto.request.UserCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.entity.user.User;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordDigest", source = "password")
    @Mapping(target = "username", source = "username")
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

}
