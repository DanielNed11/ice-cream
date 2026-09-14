package daniel.portfolio.icecream.controller.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal totalPrice
) {
}
