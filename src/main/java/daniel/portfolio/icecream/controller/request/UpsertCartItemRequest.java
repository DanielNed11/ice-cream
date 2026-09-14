package daniel.portfolio.icecream.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record UpsertCartItemRequest(
        @NotBlank String slug,
        @PositiveOrZero int quantity
) {
}
