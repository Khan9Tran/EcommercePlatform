package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.dto.response.ImageInList;
import com.hkteam.ecommerce_platform.entity.image.Image;
import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "url", target = "url")
    ImageInList toImageInList(Image productImage);
}
