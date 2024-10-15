package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.PhoneResponse;
import com.hkteam.ecommerce_platform.entity.user.User;

@Mapper(componentModel = "spring")
public interface PhoneMapper {
    @Mapping(target = "userId", source = "id")
    PhoneResponse toPhoneResponse(User user);
}
