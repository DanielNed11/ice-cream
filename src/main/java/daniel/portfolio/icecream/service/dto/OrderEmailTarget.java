package daniel.portfolio.icecream.service.dto;

import java.util.UUID;

// Everything an order email needs that isn't the body: the id for the audit
// row, the reference for the subject line, and who to send it to.
public record OrderEmailTarget(
        UUID orderId,
        String reference,
        String recipientEmail
) {
}
