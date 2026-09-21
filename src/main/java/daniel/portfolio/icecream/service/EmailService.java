package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.exception.MailSendFailedException;
import daniel.portfolio.icecream.service.dto.OrderConfirmationData;
import daniel.portfolio.icecream.model.EmailStatus;
import daniel.portfolio.icecream.model.EmailType;
import daniel.portfolio.icecream.model.SentEmail;
import daniel.portfolio.icecream.repository.OrderRepository;
import daniel.portfolio.icecream.repository.SentEmailRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final OrderRepository orderRepository;
    private final SentEmailRepository sentEmailRepository;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendOrderConfirmation(OrderConfirmationData data) {
        String reference = OrderReference.display(data.reference());

        Context context = new Context();
        context.setVariable("orderReference", reference);
        context.setVariable("items", data.items().stream().map(this:: toView).toList());
        context.setVariable("total", money(data.totalPrice()));
        context.setVariable("frontendUrl", frontendUrl);

        send(
                data.orderId(),
                EmailType.ORDER_CONFIRMATION,
                data.recipientEmail(),
                "Your order " + reference + " is confirmed",
                new EmailBody(
                        templateEngine.process("email/order-confirmation", context),
                        confirmationText(reference, data)
                )
        );
    }

    public void sendOrderDelivered(UUID orderId, String orderReference, String recipientEmail) {
        String reference = OrderReference.display(orderReference);

        Context context = new Context();
        context.setVariable("orderReference", reference);
        context.setVariable("frontendUrl", frontendUrl);

        send(
                orderId,
                EmailType.ORDER_DELIVERED,
                recipientEmail,
                "Your order " + reference + " has been delivered",
                new EmailBody(
                        templateEngine.process("email/order-delivered", context),
                        "Order " + reference + " has been delivered. Enjoy your ice cream!\n\n" + frontendUrl
                )
        );
    }

    public void sendOrderCancelled(UUID orderId, String orderReference, String recipientEmail) {
        String reference = OrderReference.display(orderReference);

        Context context = new Context();
        context.setVariable("orderReference", reference);
        context.setVariable("frontendUrl", frontendUrl);

        send(
                orderId,
                EmailType.ORDER_CANCELLED,
                recipientEmail,
                "Your order " + reference + " has been cancelled",
                new EmailBody(
                        templateEngine.process("email/order-cancelled", context),
                        "Order " + reference + " has been cancelled and the items returned to stock.\n\n" + frontendUrl
                )
        );
    }

    private static String money(BigDecimal amount) {
        return "€" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    // html and text are both long Strings that MimeMessageHelper takes in the
    // opposite order to how they read; passing them positionally meant a silent
    // swap would send the plaintext as the HTML part. Naming them removes that.
    private record EmailBody(String html, String text) {
    }

    private void send(UUID orderId, EmailType emailType, String recipient, String subject, EmailBody body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body.text(), body.html());
            mailSender.send(message);

            recordAttempt(orderId, emailType, recipient, subject, EmailStatus.SENT, null);
        } catch (Exception ex) {
            recordAttempt(orderId, emailType, recipient, subject, EmailStatus.FAILED, ex.getMessage());
            throw new MailSendFailedException(ex);
        }
    }

    private void recordAttempt(
            UUID orderId,
            EmailType emailType,
            String recipient,
            String subject,
            EmailStatus status,
            String errorMessage
    ) {
        try {
            SentEmail sentEmail = new SentEmail();
            sentEmail.setOrder(orderRepository.getReferenceById(orderId));
            sentEmail.setEmailType(emailType);
            sentEmail.setRecipient(recipient);
            sentEmail.setSubject(subject);
            sentEmail.setStatus(status);
            sentEmail.setErrorMessage(errorMessage);
            sentEmailRepository.save(sentEmail);
        } catch (Exception ex) {
            log.error("Failed to record sent_email audit row for order {}", orderId, ex);
        }
    }

    private Map<String, String> toView(OrderConfirmationData.LineItem item) {
        return Map.of(
                "productName", item.productName(),
                "quantity", String.valueOf(item.quantity()),
                "unitPrice", money(item.unitPrice()),
                "lineTotal", money(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
        );
    }

    private String confirmationText(String reference, OrderConfirmationData data) {
        StringBuilder body = new StringBuilder("Thanks for your order!\n\nOrder ")
                .append(reference)
                .append("\n\n");

        for (OrderConfirmationData.LineItem item : data.items()) {
            body.append(item.quantity())
                    .append("x ")
                    .append(item.productName())
                    .append(" - ")
                    .append(money(item.unitPrice()))
                    .append("\n");
        }

        return body.append("\nTotal: ")
                .append(money(data.totalPrice()))
                .append("\n\n")
                .append(frontendUrl)
                .toString();
    }
}
