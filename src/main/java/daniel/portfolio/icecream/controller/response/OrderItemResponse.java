package daniel.portfolio.icecream.controller.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productSlug,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
