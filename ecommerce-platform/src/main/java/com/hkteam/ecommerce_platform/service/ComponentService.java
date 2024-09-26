package com.hkteam.ecommerce_platform.service;

import java.util.List;

import com.hkteam.ecommerce_platform.dto.request.ComponentUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ComponentCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ComponentResponse;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ComponentMapper;
import com.hkteam.ecommerce_platform.repository.ComponentRepository;

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
        String name = request.getName().trim();

        if (componentRepository.existsByName(name)) {
            throw new AppException(ErrorCode.COMPONENT_EXISTED);
        }

        Component component = componentMapper.toComponent(request);

        try {
            component = componentRepository.save(component);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        return componentMapper.toComponentResponse(component);
    }

    public List<ComponentResponse> getAllComponents() {
        return componentRepository.findAll().stream()
                .map(componentMapper::toComponentResponse)
                .toList();
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

        String name = request.getName().trim();

        if (componentRepository.existsByName(name) && !component.getName().equals(name)) {
            throw new AppException(ErrorCode.COMPONENT_DUPLICATE);
        }

        component.setName(name);
        component.setRequired(request.isRequired());

        component = componentRepository.save(component);

        return componentMapper.toComponentResponse(component);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteComponent(Long id) {
        Component component =
                componentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
        componentRepository.delete(component);
    }
}
