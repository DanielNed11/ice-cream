package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.controller.response.OrderResponse;
import daniel.portfolio.icecream.exception.MailSendFailedException;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Cart;
import daniel.portfolio.icecream.model.EmailStatus;
import daniel.portfolio.icecream.model.EmailType;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.model.SentEmail;
import daniel.portfolio.icecream.repository.OrderRepository;
import daniel.portfolio.icecream.repository.ProductRepository;
import daniel.portfolio.icecream.repository.SentEmailRepository;
import daniel.portfolio.icecream.service.dto.OrderConfirmationData;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ActiveProfiles("test")
@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService sut;
    @Autowired
    private OrderService orderService;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private SentEmailRepository sentEmailRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    // Replaces the real sender, so the success path is testable without an SMTP
    // server and the failure path can be triggered on demand.
    @MockitoBean
    private JavaMailSender mailSender;

    private OrderConfirmationData confirmationFor(daniel.portfolio.icecream.model.Order order, String email) {
        return new OrderConfirmationData(
                order.getId(),
                order.getReference(),
                email,
                new BigDecimal("8.99"),
                List.of(new OrderConfirmationData.LineItem("BANANA", new BigDecimal("8.99"), 1)));
    }

    @Test
    public void aSuccessfulSendIsRecordedAsSent() {
        // Arrange
        given(mailSender.createMimeMessage()).willReturn(newMimeMessage());
        AppUser customer = testHelper.customer("mail-ok@test.local");
        var order = testHelper.order(customer, "MAIL0001", daniel.portfolio.icecream.model.OrderStatus.PLACED, "8.99");

        // Act
        sut.sendOrderConfirmation(confirmationFor(order, customer.getEmail()));

        // Assert
        List<SentEmail> audit = sentEmailRepository.findAll();
        assertEquals(1, audit.size());
        assertEquals(EmailStatus.SENT, audit.getFirst().getStatus());
        assertEquals(EmailType.ORDER_CONFIRMATION, audit.getFirst().getEmailType());
        assertEquals(customer.getEmail(), audit.getFirst().getRecipient());
        assertTrue(audit.getFirst().getSubject().contains("MAIL0001"));
    }

    @Test
    public void aFailedSendIsRecordedAsFailedAndRaises() {
        // Arrange
        given(mailSender.createMimeMessage()).willReturn(newMimeMessage());
        willThrow(new MailSendException("smtp is down")).given(mailSender).send(any(MimeMessage.class));
        AppUser customer = testHelper.customer("mail-fail@test.local");
        var order = testHelper.order(customer, "MAIL0002", daniel.portfolio.icecream.model.OrderStatus.PLACED, "8.99");

        // Act + Assert
        assertThrows(MailSendFailedException.class,
                () -> sut.sendOrderConfirmation(confirmationFor(order, customer.getEmail())));

        List<SentEmail> audit = sentEmailRepository.findAll();
        assertEquals(1, audit.size());
        assertEquals(EmailStatus.FAILED, audit.getFirst().getStatus());
        assertNotNull(audit.getFirst().getErrorMessage());
    }

    @Test
    public void checkoutSendsAConfirmationAfterTheTransactionCommits() throws Exception {
        // Arrange
        given(mailSender.createMimeMessage()).willReturn(newMimeMessage());
        AppUser customer = testHelper.customer("mail-flow@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 1);

        // Act
        OrderResponse order = orderService.checkout(customer.getId());

        // Assert: the listener is async, so wait briefly for the audit row
        SentEmail sent = awaitFirstAuditRow();
        assertEquals(EmailType.ORDER_CONFIRMATION, sent.getEmailType());
        assertEquals(EmailStatus.SENT, sent.getStatus());
        assertTrue(sent.getSubject().contains(order.reference()));
    }

    @Test
    public void aMailFailureDoesNotRollBackTheOrder() throws Exception {
        // Arrange
        given(mailSender.createMimeMessage()).willReturn(newMimeMessage());
        willThrow(new MailSendException("smtp is down")).given(mailSender).send(any(MimeMessage.class));
        AppUser customer = testHelper.customer("mail-safe@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 2);

        // Act
        OrderResponse order = orderService.checkout(customer.getId());

        // Assert: the purchase stands even though the email could not be sent
        assertEquals(1, orderRepository.count());
        assertTrue(orderRepository.findByReferenceAndAppUserId(order.reference(), customer.getId()).isPresent());
        assertEquals(8, productRepository.findBySlug("banana").orElseThrow().getStockQuantity());
        assertEquals(EmailStatus.FAILED, awaitFirstAuditRow().getStatus());
    }

    private SentEmail awaitFirstAuditRow() throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            List<SentEmail> audit = sentEmailRepository.findAll();
            if (!audit.isEmpty()) {
                return audit.getFirst();
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
        throw new AssertionError("no sent_email row was written within 5 seconds");
    }

    private MimeMessage newMimeMessage() {
        return new org.springframework.mail.javamail.JavaMailSenderImpl().createMimeMessage();
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
