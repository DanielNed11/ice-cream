package daniel.portfolio.icecream.controller.response;

import daniel.portfolio.icecream.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String reference,
        OrderStatus status,
        BigDecimal totalPrice,
        Instant placedAt,
        List<OrderItemResponse> items
) {
}
