package com.hkteam.ecommerce_platform.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hkteam.ecommerce_platform.dto.request.DefaultAddressRequest;
import com.hkteam.ecommerce_platform.dto.request.PasswordCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.UserCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.UserUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.UserDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.dto.response.UserUpdateResponse;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.UserMapper;
import com.hkteam.ecommerce_platform.repository.AddressRepository;
import com.hkteam.ecommerce_platform.repository.RoleRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;
    AddressRepository addressRepository;
    AuthenticatedUserUtil authenticatedUserUtil;

    public UserResponse createUsers(UserCreationRequest request) {

        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new AppException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        User user = userMapper.toUser(request);

        var roles = roleRepository.findByName(RoleName.USER);
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        user.setRoles(new HashSet<>(List.of(roles.get())));
        user.setPasswordDigest(passwordEncoder.encode(user.getPasswordDigest()));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDetailResponse getUser(String userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserDetailResponse(user);
    }

    public void setDefaultAddress(DefaultAddressRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        var address = addressRepository
                .findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER);
        }

        user.setDefaultAddressId(address.getId());
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<UserResponse> getAllUsers(String pageStr, String sizeStr) {
        Sort sort = Sort.by("createdAt").descending();

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sort);
        var pageData = userRepository.findAll(pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<UserResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(userMapper::toUserResponse)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        try {
            userRepository.delete(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public UserUpdateResponse updateUser(String userId, UserUpdateRequest request) {

        var user = authenticatedUserUtil.getAuthenticatedUser();

        if (!user.getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        userMapper.updateUserFromRequest(request, user);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.info("Error {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return userMapper.toUserUpdateResponse(user);
    }

    public UserDetailResponse getMyInformation() {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        var userDetail = userMapper.toUserDetailResponse(user);
        userDetail.setNoPassword(!StringUtils.hasText(user.getPasswordDigest()));

        return userDetail;
    }

    public void createPassword(PasswordCreationRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        if (StringUtils.hasText(user.getPasswordDigest())) {
            throw new AppException(ErrorCode.PASSWORD_ALREADY_CREATED);
        }

        user.setPasswordDigest(passwordEncoder.encode(request.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }
}
