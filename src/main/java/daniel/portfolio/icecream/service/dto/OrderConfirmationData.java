package daniel.portfolio.icecream.service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderConfirmationData(
        UUID orderId,
        String reference,
        String recipientEmail,
        BigDecimal totalPrice,
        List<LineItem> items
) {
    public record LineItem(
            String productName,
            BigDecimal unitPrice,
            int quantity
    ) {
    }
}
