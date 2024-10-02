package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;

import com.hkteam.ecommerce_platform.dto.response.ExternalAuthResponse;
import com.hkteam.ecommerce_platform.entity.user.ExternalAuth;

@Mapper(componentModel = "spring")
public interface ExternalAuthMapper {
    ExternalAuthResponse toExternalAuthResponse(ExternalAuth externalAuth);
}
