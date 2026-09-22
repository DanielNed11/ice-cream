package daniel.portfolio.icecream.scheduling;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.repository.OrderRepository;
import daniel.portfolio.icecream.repository.ProductRepository;
import daniel.portfolio.icecream.service.OrderService;
import daniel.portfolio.icecream.service.ProductService;
import daniel.portfolio.icecream.service.dto.OrderEmailTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class DeliverySweepTest {

    @Autowired
    private OrderService sut;
    @Autowired
    private ProductService productService;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void theSweepOnlyPicksUpPlacedOrders() {
        // Arrange
        AppUser customer = testHelper.customer("sweep@test.local");
        testHelper.order(customer, "SWEEP001", OrderStatus.PLACED, "8.99");
        testHelper.order(customer, "SWEEP002", OrderStatus.DELIVERED, "8.99");
        testHelper.order(customer, "SWEEP003", OrderStatus.CANCELLED, "8.99");

        // Act
        List<OrderEmailTarget> due = sut.findOrdersAwaitingDelivery();

        // Assert
        assertEquals(1, due.size());
        assertEquals("SWEEP001", due.getFirst().reference());
        assertEquals(customer.getEmail(), due.getFirst().recipientEmail());
    }

    @Test
    public void markOrderDeliveredMovesAPlacedOrder() {
        // Arrange
        AppUser customer = testHelper.customer("mark@test.local");
        Order order = testHelper.order(customer, "MARK0001", OrderStatus.PLACED, "8.99");

        // Act
        boolean marked = sut.markOrderDelivered(order.getId());

        // Assert
        assertTrue(marked);
        assertEquals(OrderStatus.DELIVERED, orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    // This is what stops a customer who cancels mid-run from being told their
    // order was delivered: the job only sends when the mark succeeds.
    @Test
    public void markOrderDeliveredRefusesAnOrderThatLeftPlaced() {
        // Arrange
        AppUser customer = testHelper.customer("raced@test.local");
        Order cancelled = testHelper.order(customer, "RACE0001", OrderStatus.CANCELLED, "8.99");

        // Act
        boolean marked = sut.markOrderDelivered(cancelled.getId());

        // Assert
        assertFalse(marked);
        assertEquals(OrderStatus.CANCELLED, orderRepository.findById(cancelled.getId()).orElseThrow().getStatus());
    }

    @Test
    public void markOrderDeliveredIsNotRepeatable() {
        // Arrange
        AppUser customer = testHelper.customer("twice@test.local");
        Order order = testHelper.order(customer, "TWICE001", OrderStatus.PLACED, "8.99");
        sut.markOrderDelivered(order.getId());

        // Act
        boolean second = sut.markOrderDelivered(order.getId());

        // Assert
        assertFalse(second);
    }

    @Test
    public void restockToppedUpActiveProductsOnly() {
        // Arrange
        Product active = testHelper.product("banana", "8.99", 5);
        Product withdrawn = testHelper.product("pistachio", "9.49", 5, false);

        // Act
        int restocked = productService.restockActiveProducts(10);

        // Assert
        assertEquals(1, restocked);
        assertEquals(15, productRepository.findById(active.getId()).orElseThrow().getStockQuantity());
        assertEquals(5, productRepository.findById(withdrawn.getId()).orElseThrow().getStockQuantity());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
