package daniel.portfolio.icecream.controller.response;

import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminOrderResponse(
        UUID id,
        String reference,
        Instant placedAt,
        OrderStatus status,
        String customerEmail,
        BigDecimal totalPrice
) {
    public AdminOrderResponse(Order order) {
        this(
                order.getId(),
                order.getReference(),
                order.getCreatedAt(),
                order.getStatus(),
                order.getAppUser().getEmail(),
                order.getTotalPrice()
        );
    }
}
