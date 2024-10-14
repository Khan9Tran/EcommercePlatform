package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.dto.response.CustomerResponse;
import org.mapstruct.*;

import com.hkteam.ecommerce_platform.dto.request.UserCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.UserUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.UserDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.dto.response.UserUpdateResponse;
import com.hkteam.ecommerce_platform.entity.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordDigest", source = "password")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "name", expression = "java(request.getFirstName() + \" \" + request.getLastName())")
    User toUser(UserCreationRequest request);

    @Mapping(target = "externalAuth", source = "externalAuth")
    UserResponse toUserResponse(User user);

    @Mapping(target = "isBlocked", source = "blocked")
    UserDetailResponse toUserDetailResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", expression = "java(request.getFirstName() + \" \" + request.getLastName())")
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);

    UserUpdateResponse toUserUpdateResponse(User user);

    CustomerResponse toCustomerResponse(User user);
}
