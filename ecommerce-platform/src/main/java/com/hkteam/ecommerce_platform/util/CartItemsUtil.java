package com.hkteam.ecommerce_platform.util;

import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class CartItemsUtil {
    public boolean isAvailable(CartItem cartItem) {
        if (cartItem.getProduct().isBlocked() || !cartItem.getProduct().isAvailable()) {
            return false;
        }
        if (Objects.isNull(cartItem.getVariant())) {
            return cartItem.getQuantity() <= cartItem.getProduct().getQuantity();
        }

        return cartItem.getQuantity() <= cartItem.getVariant().getQuantity();
    }
}
