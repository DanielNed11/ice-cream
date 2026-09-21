package daniel.portfolio.icecream.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank @Size(max = 50) @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") String slug,
        @NotBlank @Size(max = 255) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @PositiveOrZero int stockQuantity
) {
}
