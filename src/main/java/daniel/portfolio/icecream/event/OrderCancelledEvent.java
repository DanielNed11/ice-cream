package daniel.portfolio.icecream.event;

import java.util.UUID;

public record OrderCancelledEvent(UUID orderId) {
}
