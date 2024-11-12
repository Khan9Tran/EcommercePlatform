package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.response.CartItemDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.CartResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.CartItemMapper;
import com.hkteam.ecommerce_platform.mapper.CartMapper;
import com.hkteam.ecommerce_platform.repository.CartItemRepository;
import com.hkteam.ecommerce_platform.repository.CartRepository;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.repository.VariantRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {
    CartMapper cartMapper;
    CartItemMapper cartItemMapper;
    CartRepository cartRepository;
    AuthenticatedUserUtil authenticatedUserUtil;


    @PreAuthorize("hasRole('USER')")
    public PaginationResponse<CartResponse> getCarts(String pageStr, String sizeStr) {

        Sort sort = Sort.by("lastUpdatedAt").descending();

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sort);

        var pageData = cartRepository.findByUserAndIsAvailable(authenticatedUserUtil.getAuthenticatedUser(), true, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        List<CartResponse> cartResponses = new ArrayList<>();
        pageData.getContent().forEach(
                cart -> {
                    CartResponse cartResponse = cartMapper.toCartResponse(cart);
                    cartResponses.add(setItems(cartResponse, cart));
                }
        );

        return PaginationResponse.<CartResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(cartResponses)
                .build();
    }

    Set<CartItem> filterNotCheckout(Set<CartItem> cartItems) {
        return cartItems.stream().filter(cartItem -> !cartItem.isCheckout()).collect(Collectors.toSet());
    }

    CartResponse setItems(CartResponse cartResponse, Cart cart) {
        cartResponse.setItems(filterNotCheckout(cart.getCartItems()).stream()
            .map(cartItem ->
                    {
                        CartItemDetailResponse cartItemDetailResponse = cartItemMapper.toCartItemDetailResponse(cartItem);
                        cartItemDetailResponse.setAvailable(isAvailable(cartItem));

                        if (!Objects.isNull(cartItem.getVariant())) {
                            cartItemDetailResponse.setSalePrice(cartItem.getVariant().getSalePrice());
                            cartItemDetailResponse.setImage(cartItem.getVariant().getImageUrl());
                            cartItemDetailResponse.setOriginalPrice(cartItem.getVariant().getOriginalPrice());
                            cartItemDetailResponse.setVariantId(cartItem.getVariant().getId());
                            cartItemDetailResponse.setVariantSlug(cartItem.getVariant().getSlug());
                            cartItemDetailResponse.setValue(cartItem.getVariant().getValues().stream().map(value -> value.getValue()).collect(Collectors.toList()));
                        }
                        return  cartItemDetailResponse;

                    }
            )
            .collect(Collectors
                    .toList()));

        return cartResponse;
    }

    boolean isAvailable(CartItem cartItem) {
        if (cartItem.getProduct().isBlocked()  || !cartItem.getProduct().isAvailable()){
            return false;
        }
        if (Objects.isNull(cartItem.getVariant())) {
            return  cartItem.getQuantity() >= cartItem.getVariant().getQuantity();
        }
        return  cartItem.getQuantity() >= cartItem.getProduct().getQuantity();
    }

    @PreAuthorize("hasRole('USER')")
    public Void deleteCart(Long id) {
        var cart = cartRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (!cart.getUser().equals(authenticatedUserUtil.getAuthenticatedUser())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        try {
            cartRepository.delete(cart);
        }
        catch (Exception e)
        {
            log.error("Error when delete cart: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }
}
