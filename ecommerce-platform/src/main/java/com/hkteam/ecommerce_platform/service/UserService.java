package com.hkteam.ecommerce_platform.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.DefaultAddressRequest;
import com.hkteam.ecommerce_platform.dto.request.UserCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.UserDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.UserMapper;
import com.hkteam.ecommerce_platform.repository.AddressRepository;
import com.hkteam.ecommerce_platform.repository.RoleRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;

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

    public UserResponse createUsers(UserCreationRequest request) {

        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new AppException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        User user = userMapper.toUser(request);

        String fullName = request.getFirstName() + " " + request.getLastName();

        user.setName(fullName);

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        var user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var address = addressRepository
                .findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER);
        }

        user.setDefaultAddressId(address.getId());
        userRepository.save(user);
    }
}
