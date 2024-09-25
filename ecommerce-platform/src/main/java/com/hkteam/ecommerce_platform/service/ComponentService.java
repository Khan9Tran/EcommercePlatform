package com.hkteam.ecommerce_platform.service;

import java.util.List;
import java.util.stream.Collectors;

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

    public ComponentResponse createComponent(ComponentCreationRequest request) {
        Component component = componentMapper.toComponent(request);
        component = componentRepository.save(component);
        return componentMapper.toComponentResponse(component);
    }

    public List<ComponentResponse> getAllComponents() {
        List<Component> components = componentRepository.findAll();
        return components.stream().map(componentMapper::toComponentResponse).collect(Collectors.toList());
    }

    public ComponentResponse getOneComponentById(Long id) {
        Component component =
                componentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
        return componentMapper.toComponentResponse(component);
    }

    public ComponentResponse updateComponent(Long id, ComponentCreationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.NAME_NOT_BLANK);
        }
        Component component =
                componentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
        component.setName(request.getName());
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
