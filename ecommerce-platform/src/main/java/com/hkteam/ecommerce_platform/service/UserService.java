package com.hkteam.ecommerce_platform.service;

import java.util.HashSet;
import java.util.List;

import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hkteam.ecommerce_platform.dto.request.*;
import com.hkteam.ecommerce_platform.dto.response.*;
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
    private final ProductRepository productRepository;
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
        var res = userMapper.toUserDetailResponse(user);
        res.setCartItemCount(user.getCarts().stream()
                .mapToInt(cart -> cart.getCartItems().size())
                .sum());
        return res;
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
        userDetail.setCartItemCount(user.getCarts().stream()
                .mapToInt(cart -> cart.getCartItems().size())
                .sum());

        return userDetail;
    }

    public void createPassword(PasswordCreationRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        if (StringUtils.hasText(user.getPasswordDigest())) {
            throw new AppException(ErrorCode.PASSWORD_ALREADY_CREATED);
        }

        setPassword(user, request.getPassword());
    }

    public void updatePassword(ChangePasswordRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordDigest())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirmation())) {
            throw new AppException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        setPassword(user, request.getNewPassword());
    }

    public void setPassword(User user, String password) {
        user.setPasswordDigest(passwordEncoder.encode(password));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<CustomerResponse> getAllCustomers(
            String pageStr, String sizeStr, String tab, String sort, String search) {
        Sort sortable =
                switch (sort) {
                    case "newest" -> Sort.by("createdAt").descending();
                    case "oldest" -> Sort.by("createdAt").ascending();
                    case "az" -> Sort.by("name").ascending();
                    case "za" -> Sort.by("name").descending();
                    default -> Sort.unsorted();
                };

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        Page<User> pageData = null;

        try {
            pageData = switch (tab) {
                case "all" -> userRepository.search(List.of(RoleName.USER), search, search, pageable);
                case "blocked" -> userRepository.searchByBlocked(
                        List.of(RoleName.USER), List.of(true), search, search, pageable);
                case "active" -> userRepository.searchByBlocked(
                        List.of(RoleName.USER), List.of(false), search, search, pageable);
                default -> throw new AppException(ErrorCode.INVALID_REQUEST);};
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        int page = Integer.parseInt(pageStr);

        assert pageData != null;
        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<CustomerResponse>builder()
                .currentPage(Integer.parseInt(pageStr))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(userMapper::toCustomerResponse)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeStatusAccount(UserAccountRequest request) {
        if (!passwordEncoder.matches(
                request.getPassword(),
                authenticatedUserUtil.getAuthenticatedUser().getPasswordDigest()))
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);

        var customer = userRepository
                .findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        customer.setBlocked(!customer.isBlocked());

        try {
            userRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<AdminResponse> getAllAdmins(
            String pageStr, String sizeStr, String tab, String sort, String search) {
        Sort sortable =
                switch (sort) {
                    case "newest" -> Sort.by("createdAt").descending();
                    case "oldest" -> Sort.by("createdAt").ascending();
                    case "az" -> Sort.by("name").ascending();
                    case "za" -> Sort.by("name").descending();
                    default -> Sort.unsorted();
                };

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        Page<User> pageData = null;

        try {
            pageData = switch (tab) {
                case "all" -> userRepository.search(List.of(RoleName.ADMIN), search, search, pageable);
                case "blocked" -> userRepository.searchByBlocked(
                        List.of(RoleName.ADMIN), List.of(true), search, search, pageable);
                case "active" -> userRepository.searchByBlocked(
                        List.of(RoleName.ADMIN), List.of(false), search, search, pageable);
                default -> throw new AppException(ErrorCode.INVALID_REQUEST);};
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        int page = Integer.parseInt(pageStr);

        assert pageData != null;
        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<AdminResponse>builder()
                .currentPage(Integer.parseInt(pageStr))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(userMapper::toAdminResponse)
                        .toList())
                .build();
    }

    public UserInfoResponse getUserInfo() {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        boolean isAdmin = false;
        boolean isSeller = false;

        for (Role role : user.getRoles()) {
            if (role.getName().equals(RoleName.ADMIN)) {
                isAdmin = true;
            }
            if (role.getName().equals(RoleName.SELLER)) {
                isSeller = true;
            }
        }

        if (isAdmin) {
            return UserInfoResponse.builder()
                    .name(user.getName())
                    .lastRole("ADMIN")
                    .imageUrl(user.getImageUrl())
                    .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                    .build();
        } else if (isSeller) {
            return UserInfoResponse.builder()
                    .name(user.getName())
                    .lastRole("SELLER")
                    .imageUrl(user.getImageUrl())
                    .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                    .build();
        } else {
            return UserInfoResponse.builder()
                    .name(user.getName())
                    .lastRole("USER")
                    .imageUrl(user.getImageUrl())
                    .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                    .build();
        }

    }

    public UserFollowProductResponse followProduct(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (Boolean.FALSE.equals(product.isAvailable()) || Boolean.TRUE.equals(product.isBlocked())) {
            throw  new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (user.getFollowingProducts().contains(product)) {
            throw new AppException(ErrorCode.PRODUCT_HAS_FOLLOWED);
        }

        if (user.getFollowingProducts().size() >= 40) {
            throw  new AppException(ErrorCode.LIMIT_FOLLOW_40_PRODUCT);
        }
        user.getFollowingProducts().add(product);
        userRepository.save(user);

        return  UserFollowProductResponse.builder().productId(productId).userId(user.getId()).build();
    }
}
