package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.*;

import com.hkteam.ecommerce_platform.dto.request.CategoryCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.CategoryResponse;
import com.hkteam.ecommerce_platform.entity.category.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "iconUrl", ignore = true)
    Category toCategory(CategoryCreationRequest request);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(target = "listComponent", source = "components")
    CategoryResponse toCategoryResponse(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);
}
