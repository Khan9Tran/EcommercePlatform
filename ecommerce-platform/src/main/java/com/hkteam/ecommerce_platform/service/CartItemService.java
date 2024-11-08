package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.request.CartItemCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.CartItemResponse;
import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.product.Variant;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

    @PreAuthorize("hasAuthority('PERMISSION_PURCHASE')")
    public CartItemResponse addProductToCart(CartItemCreationRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        var ci = cartItemRepository.findByProductIdAndCartUser(request.getProductId(),user);
        var product = productRepository.findById(request.getProductId()).orElseThrow(()-> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Variant variant = null;

        if (!product.isAvailable() && product.isBlocked()) throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

        if (!request.getVariantId().isEmpty()) {
            variant = variantRepository.findById(request.getVariantId()).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            if (!variant.getProduct().equals(product))
                throw new AppException(ErrorCode.UNAUTHORIZED);

            if (!variant.isAvailable()) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }

        if (!isAvailableQuantity(product, variant, request.getQuantity()))
            throw  new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);

        CartItem cartItem;

        if (ci.isPresent()) {
            cartItem = addQuantityForCartItem(ci.get(), product, variant, request.getQuantity());
        }
        else {
                var cart = cartRepository.findByUserAndStore(user, product.getStore()).orElse(
                        Cart.builder()
                                .user(user)
                                .isAvailable(Boolean.TRUE)
                                .store(product.getStore())
                                .build()
                );
                cartItem = addCartItemToCart(cart, product, variant, request.getQuantity());

        }

        try {
            cartItemRepository.save(cartItem);
        }
        catch (Exception e)
        {
            log.error("Error when create item: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return  cartItemMapper.toCartItemResponse(cartItem);

    }

    private boolean isAvailableQuantity(Product product, Variant variant, int quantity){
        if (Objects.isNull(variant)) {
            return  product.getQuantity() >= quantity;
        }
        return variant.getQuantity() >= quantity;
    }

    private CartItem addCartItemToCart(Cart cart, Product product, Variant variant, int quantity) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .variant(variant)
                .product(product)
                .quantity(quantity)
                .isAvailable(Boolean.TRUE)
                .build();

        return cartItem;
    }

    private CartItem addQuantityForCartItem(CartItem cartItem, Product product, Variant variant, int quantity) {
       cartItem.setQuantity(cartItem.getQuantity() + quantity);

       return  cartItem;
    }



}
