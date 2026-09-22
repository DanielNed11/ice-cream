package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.controller.response.OrderResponse;
import daniel.portfolio.icecream.exception.EmptyCartException;
import daniel.portfolio.icecream.exception.InsufficientStockException;
import daniel.portfolio.icecream.exception.OrderNotCancellableException;
import daniel.portfolio.icecream.exception.OrderNotFoundException;
import daniel.portfolio.icecream.exception.ProductNotAvailableException;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Cart;
import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.repository.CartItemRepository;
import daniel.portfolio.icecream.repository.OrderRepository;
import daniel.portfolio.icecream.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService sut;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    public void checkoutSnapshotsPricesDecrementsStockAndClearsTheCart() {
        // Arrange
        AppUser customer = testHelper.customer("checkout@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 3);

        // Act
        OrderResponse order = sut.checkout(customer.getId());

        // Assert
        assertEquals(new BigDecimal("26.97"), order.totalPrice());
        assertEquals(OrderStatus.PLACED, order.status());
        assertNotNull(order.reference());
        assertEquals(1, order.items().size());
        assertEquals(new BigDecimal("8.99"), order.items().getFirst().unitPrice());
        assertEquals(7, productRepository.findBySlug("banana").orElseThrow().getStockQuantity());
        assertTrue(cartItemRepository.findByCartId(cart.getId()).isEmpty());
    }

    @Test
    public void checkoutKeepsThePriceThatWasChargedAfterTheProductIsRepriced() {
        // Arrange
        AppUser customer = testHelper.customer("snapshot@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 1);
        OrderResponse order = sut.checkout(customer.getId());

        // Act
        banana.setPrice(new BigDecimal("99.99"));
        productRepository.save(banana);

        // Assert
        OrderResponse reloaded = sut.findOrder(customer.getId(), order.reference());
        assertEquals(new BigDecimal("8.99"), reloaded.items().getFirst().unitPrice());
        assertEquals(new BigDecimal("8.99"), reloaded.totalPrice());
    }

    @Test
    public void checkoutWithAnEmptyCartIsRejected() {
        // Arrange
        AppUser customer = testHelper.customer("emptycart@test.local");
        testHelper.cart(customer);

        // Act + Assert
        assertThrows(EmptyCartException.class, () -> sut.checkout(customer.getId()));
    }

    @Test
    public void checkoutWithoutEnoughStockIsRejectedAndNothingIsPersisted() {
        // Arrange
        AppUser customer = testHelper.customer("nostock@test.local");
        Product banana = testHelper.product("banana", "8.99", 2);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 5);

        // Act + Assert
        assertThrows(InsufficientStockException.class, () -> sut.checkout(customer.getId()));
        assertEquals(2, productRepository.findBySlug("banana").orElseThrow().getStockQuantity());
        assertEquals(0, orderRepository.count());
    }

    @Test
    public void checkoutWithADeactivatedProductIsRejected() {
        // Arrange
        AppUser customer = testHelper.customer("inactive@test.local");
        Product banana = testHelper.product("banana", "8.99", 10, false);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 1);

        // Act + Assert
        assertThrows(ProductNotAvailableException.class, () -> sut.checkout(customer.getId()));
        assertEquals(0, orderRepository.count());
    }

    @Test
    public void cancellingRestoresStock() {
        // Arrange
        AppUser customer = testHelper.customer("cancel@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 4);
        OrderResponse order = sut.checkout(customer.getId());

        // Act
        OrderResponse cancelled = sut.cancel(customer.getId(), order.reference());

        // Assert
        assertEquals(OrderStatus.CANCELLED, cancelled.status());
        assertEquals(10, productRepository.findBySlug("banana").orElseThrow().getStockQuantity());
    }

    @Test
    public void cancellingTwiceDoesNotRestoreStockTwice() {
        // Arrange
        AppUser customer = testHelper.customer("double@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        Cart cart = testHelper.cart(customer);
        testHelper.cartItem(cart, banana, 4);
        OrderResponse order = sut.checkout(customer.getId());
        sut.cancel(customer.getId(), order.reference());

        // Act + Assert
        assertThrows(OrderNotCancellableException.class,
                () -> sut.cancel(customer.getId(), order.reference()));
        assertEquals(10, productRepository.findBySlug("banana").orElseThrow().getStockQuantity());
    }

    @Test
    public void aDeliveredOrderCannotBeCancelled() {
        // Arrange
        AppUser customer = testHelper.customer("delivered@test.local");
        Order order = testHelper.order(customer, "DELIV001", OrderStatus.DELIVERED, "8.99");

        // Act + Assert
        assertThrows(OrderNotCancellableException.class,
                () -> sut.cancel(customer.getId(), order.getReference()));
    }

    @Test
    public void oneCustomerCannotReadOrCancelAnothersOrder() {
        // Arrange
        AppUser owner = testHelper.customer("owner@test.local");
        AppUser stranger = testHelper.customer("stranger@test.local");
        Order order = testHelper.order(owner, "OWNED001", OrderStatus.PLACED, "8.99");

        // Act + Assert
        assertThrows(OrderNotFoundException.class,
                () -> sut.findOrder(stranger.getId(), order.getReference()));
        assertThrows(OrderNotFoundException.class,
                () -> sut.cancel(stranger.getId(), order.getReference()));
    }

    @Test
    public void aPageBeyondTheLastStillReportsTheRealTotal() {
        // Arrange
        AppUser customer = testHelper.customer("paging@test.local");
        testHelper.order(customer, "PAGE0001", OrderStatus.PLACED, "8.99");
        testHelper.order(customer, "PAGE0002", OrderStatus.PLACED, "8.99");

        // Act
        Page<OrderResponse> beyondTheEnd = sut.findOrders(customer.getId(), PageRequest.of(9, 10));

        // Assert
        assertTrue(beyondTheEnd.getContent().isEmpty());
        assertEquals(2, beyondTheEnd.getTotalElements());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
