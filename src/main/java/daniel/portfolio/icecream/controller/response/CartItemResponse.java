package daniel.portfolio.icecream.controller.response;

import java.math.BigDecimal;

public record CartItemResponse(
        String productSlug,
        String productName,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal
) {
}
