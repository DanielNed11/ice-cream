package daniel.portfolio.icecream.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;

import static daniel.portfolio.icecream.constants.Constants.MAX_CART_ITEM_QUANTITY;
import jakarta.validation.constraints.PositiveOrZero;

public record UpsertCartItemRequest(
        @NotBlank String slug,
        @PositiveOrZero @Max(MAX_CART_ITEM_QUANTITY) int quantity
) {
}
