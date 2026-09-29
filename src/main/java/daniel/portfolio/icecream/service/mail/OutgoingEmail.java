package daniel.portfolio.icecream.service.mail;

import daniel.portfolio.icecream.model.EmailType;

import java.util.UUID;

public record OutgoingEmail(
        UUID orderId,
        EmailType emailType,
        String recipient,
        String subject,
        String html,
        String text
) {
}
