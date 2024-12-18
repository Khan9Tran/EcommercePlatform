package com.hkteam.ecommerce_platform.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ComponentCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ComponentUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ComponentResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ComponentMapper;
import com.hkteam.ecommerce_platform.repository.ComponentRepository;
import com.hkteam.ecommerce_platform.util.PageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ComponentService {
    ComponentRepository componentRepository;
    ComponentMapper componentMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public ComponentResponse createComponent(ComponentCreationRequest request) {
        if (componentRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.COMPONENT_EXISTED);
        }

        Component component = componentMapper.toComponent(request);

        try {
            componentRepository.save(component);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while creating component: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return componentMapper.toComponentResponse(component);
    }

    public PaginationResponse<ComponentResponse> getAllComponents(String pageStr, String sizeStr, String search) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sort);

        var pageData = componentRepository.findByNameContainsIgnoreCase(search, pageable);
        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<ComponentResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(componentMapper::toComponentResponse)
                        .toList())
                .build();
    }

    public ComponentResponse getOneComponentById(Long id) {
        Component component =
                componentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
        return componentMapper.toComponentResponse(component);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ComponentResponse updateComponent(Long id, ComponentUpdateRequest request) {
        Component component =
                componentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));

        boolean isDuplicateName = componentRepository.existsByNameIgnoreCase(request.getName())
                && !component.getName().equalsIgnoreCase(request.getName());
        if (isDuplicateName) {
            throw new AppException(ErrorCode.COMPONENT_DUPLICATE);
        }

        componentMapper.updateComponentFromRequest(request, component);

        try {
            componentRepository.save(component);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating component: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return componentMapper.toComponentResponse(component);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteComponent(Long id) {
        Component component =
                componentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
        componentRepository.delete(component);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ComponentResponse> getAllComponents() {
        return componentRepository.findAll(Sort.by("name").ascending()).stream()
                .map(componentMapper::toComponentResponse)
                .toList();
    }

    public List<ComponentResponse> getAllComponentsByCategoryId(Long id) {
        return componentRepository.findByCategoriesId(id).stream()
                .map(componentMapper::toComponentResponse)
                .toList();
    }
}
