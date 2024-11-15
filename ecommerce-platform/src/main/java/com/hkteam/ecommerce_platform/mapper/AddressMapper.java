package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.*;

import com.hkteam.ecommerce_platform.dto.request.AddressCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.AddressUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.AddressResponse;
import com.hkteam.ecommerce_platform.entity.user.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address toAddress(AddressCreationRequest request);

    @Mapping(target = "user", source = "user")
    AddressResponse toAddressResponse(Address address);

    void updateAddressFromRequest(AddressUpdateRequest request, @MappingTarget Address address);
}
