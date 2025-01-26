package com.hkteam.ecommerce_platform.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.CartItemCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.CartItemUpdateQuantityRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.CartItemMapper;
import com.hkteam.ecommerce_platform.repository.CartItemRepository;
import com.hkteam.ecommerce_platform.repository.CartRepository;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.repository.VariantRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartItemService {
    VariantRepository variantRepository;
    CartItemRepository cartItemRepository;
    CartRepository cartRepository;
    ProductRepository productRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    CartItemMapper cartItemMapper;

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public CartItemResponse addProductToCart(CartItemCreationRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (countItem(user) >= 100) {
            throw new AppException(ErrorCode.HAS_MORE_PRODUCT_IN_CART);
        }

        Optional<CartItem> ci;
        if (request.getVariantId().isEmpty()) {
            ci = cartItemRepository.findByProductIdAndCartUserAndCartCartItemsAndIsCheckout(
                    request.getProductId(), user, false);
        } else {
            ci = cartItemRepository.findByVariantIdAndCartUserAndCartCartItemsAndIsCheckout(
                    request.getVariantId(), user, false);
        }
        var product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Variant variant = null;

        if (!product.isAvailable() && product.isBlocked()) throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

        if (!request.getVariantId().isEmpty()) {
            variant = variantRepository
                    .findById(request.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            if (!variant.getProduct().equals(product)) throw new AppException(ErrorCode.UNAUTHORIZED);

            if (!variant.isAvailable()) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }

        if (isNotAvailableQuantity(product, variant, request.getQuantity()))
            throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);

        log.info("HAHAHAHAHHA");
        CartItem cartItem;

        if (ci.isPresent() && (variant == null || ci.get().getVariant().equals(variant))) {
            cartItem = addQuantityForCartItem(ci.get(), request.getQuantity());
            cartItem.getCart().setAvailable(Boolean.TRUE);
            if (isNotAvailableQuantity(cartItem.getProduct(), cartItem.getVariant(), cartItem.getQuantity()))
                throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);
        } else {
            var cart = cartRepository
                    .findByUserAndStore(user, product.getStore())
                    .orElse(Cart.builder()
                            .user(user)
                            .isAvailable(Boolean.TRUE)
                            .store(product.getStore())
                            .build());
            cart.setAvailable(Boolean.TRUE);
            cartItem = addCartItemToCart(cart, product, variant, request.getQuantity());
        }

        try {
            cartItemRepository.save(cartItem);
        } catch (Exception e) {
            log.error("Error when create item: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return cartItemMapper.toCartItemResponse(cartItem);
    }

    boolean isNotAvailableQuantity(Product product, Variant variant, int quantity) {
        if (Objects.isNull(variant)) {
            return product.getQuantity() < quantity;
        }
        return variant.getQuantity() < quantity;
    }

    private CartItem addCartItemToCart(Cart cart, Product product, Variant variant, int quantity) {
        return CartItem.builder()
                .cart(cart)
                .variant(variant)
                .product(product)
                .quantity(quantity)
                .build();
    }

    private CartItem addQuantityForCartItem(CartItem cartItem, int quantity) {
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        return cartItem;
    }

    @PreAuthorize("hasRole('USER')")
    public CartItemResponse changeQuantity(CartItemUpdateQuantityRequest request, Long id) {
        if (request.getQuantity() < 0) {
            throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);
        } else if (request.getQuantity() == 0) {
            deleteCartItem(id);
            return null;
        }

        var cartItem =
                cartItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!authenticatedUserUtil
                .getAuthenticatedUser()
                .equals(cartItem.getCart().getUser())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (request.getQuantity() > cartItem.getQuantity()
                && isNotAvailableQuantity(cartItem.getProduct(), cartItem.getVariant(), request.getQuantity()))
            throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);

        cartItem.setQuantity(request.getQuantity());

        try {
            cartItemRepository.save(cartItem);
        } catch (Exception e) {
            log.error("Error when update item: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return cartItemMapper.toCartItemResponse(cartItem);
    }

    @Transactional
    public void deleteCartItem(Long id) {
        var cartItem =
                cartItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        if (!authenticatedUserUtil
                .getAuthenticatedUser()
                .equals(cartItem.getCart().getUser())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        log.info("Delete cart item: {}", cartItem.getId());
        try {
            Cart cart = cartItem.getCart();
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);

            if (cart.getCartItems().isEmpty()) {
                cartRepository.delete(cart);
            }

        } catch (Exception e) {
            log.error("Error when delete item: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    private Integer countItem(User user) {
        return user.getCarts().stream()
                .mapToInt(cart -> cart.getCartItems().stream()
                        .filter(cartItem -> !cartItem.isCheckout())
                        .mapToInt(CartItem::getQuantity)
                        .sum())
                .sum();
    }

    public QuantityCartItemsResponse countCartItems() {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        return QuantityCartItemsResponse.builder().quantity(countItem(user)).build();
    }

    public List<MiniCartItemResponse> getCartItemNewest() {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        Pageable pageable = PageRequest.of(0, 5);
        return cartItemRepository.findByCart_UserAndIsCheckoutFalse(user, pageable).stream()
                .map(cartItem -> MiniCartItemResponse.builder()
                        .id(cartItem.getId())
                        .name(cartItem.getProduct().getName())
                        .slug(cartItem.getProduct().getSlug())
                        .image(cartItem.getProduct().getMainImageUrl())
                        .value(
                                cartItem.getVariant() != null
                                        ? cartItem.getVariant().getValues().stream()
                                                .map(Value::getValue)
                                                .toList()
                                        : null)
                        .salePrice(
                                cartItem.getVariant() != null
                                        ? cartItem.getVariant().getSalePrice()
                                        : cartItem.getProduct().getSalePrice())
                        .build())
                .toList();
    }

    public Integer getQuantityCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        int quantity = 0;

        if (Objects.nonNull(cartItem.getVariant()) && cartItem.getVariant().getQuantity() > 0) {
            quantity = cartItem.getVariant().getQuantity();
        }

        if (Objects.isNull(cartItem.getVariant()) && cartItem.getProduct().getQuantity() > 0) {
            quantity = cartItem.getProduct().getQuantity();
        }

        return quantity;
    }
}
