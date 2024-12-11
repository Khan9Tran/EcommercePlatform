package com.hkteam.ecommerce_platform.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.response.CartItemDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.CartResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.CartItemMapper;
import com.hkteam.ecommerce_platform.mapper.CartMapper;
import com.hkteam.ecommerce_platform.repository.CartRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.CartItemsUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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

        var pageData =
                cartRepository.findByUserAndIsAvailable(authenticatedUserUtil.getAuthenticatedUser(), true, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        List<CartResponse> cartResponses = new ArrayList<>();
        pageData.getContent().forEach(cart -> {
            CartResponse cartResponse = cartMapper.toCartResponse(cart);
            cartResponses.add(setItems(cartResponse, cart));
        });

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

    List<CartItem> filterNotCheckout(List<CartItem> cartItems) {
        return cartItems.stream()
                .filter(cartItem -> !cartItem.isCheckout())
                .sorted(Comparator.comparing(CartItem::getCreatedAt).reversed())
                .toList();
    }

    CartResponse setItems(CartResponse cartResponse, Cart cart) {
        cartResponse.setItems(filterNotCheckout(cart.getCartItems()).stream()
                .map(cartItem -> {
                    CartItemDetailResponse cartItemDetailResponse = cartItemMapper.toCartItemDetailResponse(cartItem);
                    cartItemDetailResponse.setAvailable(CartItemsUtil.isAvailable(cartItem));

                    if (!Objects.isNull(cartItem.getVariant())) {
                        cartItemDetailResponse.setSalePrice(
                                cartItem.getVariant().getSalePrice());
                        cartItemDetailResponse.setImage(cartItem.getProduct().getMainImageUrl());
                        cartItemDetailResponse.setOriginalPrice(
                                cartItem.getVariant().getOriginalPrice());
                        cartItemDetailResponse.setVariantId(
                                cartItem.getVariant().getId());
                        cartItemDetailResponse.setVariantSlug(
                                cartItem.getVariant().getSlug());
                        cartItemDetailResponse.setValue(cartItem.getVariant().getValues().stream()
                                .map(Value::getValue)
                                .toList());
                    }
                    return cartItemDetailResponse;
                })
                .toList());

        return cartResponse;
    }

    @PreAuthorize("hasRole('USER')")
    public void deleteCart(Long id) {
        var cart = cartRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (!cart.getUser().equals(authenticatedUserUtil.getAuthenticatedUser())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        try {
            cartRepository.delete(cart);
        } catch (Exception e) {
            log.error("Error when delete cart: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }
}
