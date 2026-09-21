package daniel.portfolio.icecream.controller.response;

import java.math.BigDecimal;

public record ProductResponse(
        String slug,
        String name,
        BigDecimal price,
        int stockQuantity,
        boolean active
) {
}
