package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.entity.user.User;

@Mapper(componentModel = "spring")
public interface EmailMapper {
    @Mapping(target = "userId", source = "id")
    EmailResponse toEmailResponse(User user);
}
