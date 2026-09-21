package daniel.portfolio.icecream.repository.projection;

import daniel.portfolio.icecream.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface OrderExportProjection {

    String getReference();
    Instant getPlacedAt();
    OrderStatus getStatus();
    String getCustomerEmail();
    String getProductName();
    int getQuantity();
    BigDecimal getUnitPrice();
    UUID getItemId();
}
