package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.ImageInList;
import com.hkteam.ecommerce_platform.entity.image.Image;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "url", target = "url")
    ImageInList toImageInList(Image productImage);
}
