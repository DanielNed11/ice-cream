package daniel.portfolio.icecream.controller.response;

import daniel.portfolio.icecream.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        String productSlug,
        String productName,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal
) {
    public CartItemResponse(CartItem cartItem) {
        this(
                cartItem.getProduct().getSlug(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
    }
}
