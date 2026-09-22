package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.controller.response.CartResponse;
import daniel.portfolio.icecream.exception.ProductNotAvailableException;
import daniel.portfolio.icecream.exception.ProductNotFoundException;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class CartServiceTest {

    @Autowired
    private CartService sut;
    @Autowired
    private TestHelper testHelper;

    @Test
    public void addingAProductCreatesTheCartAndTheLine() {
        // Arrange
        AppUser customer = testHelper.customer("add@test.local");
        testHelper.product("banana", "8.99", 10);

        // Act
        CartResponse cart = sut.upsertItem(customer.getId(), "banana", 2);

        // Assert
        assertEquals(1, cart.items().size());
        assertEquals(2, cart.items().getFirst().quantity());
        assertEquals(new BigDecimal("17.98"), cart.totalPrice());
    }

    @Test
    public void upsertingReplacesTheQuantityRatherThanAddingToIt() {
        // Arrange
        AppUser customer = testHelper.customer("replace@test.local");
        testHelper.product("banana", "8.99", 10);
        sut.upsertItem(customer.getId(), "banana", 2);

        // Act
        CartResponse cart = sut.upsertItem(customer.getId(), "banana", 5);

        // Assert
        assertEquals(1, cart.items().size());
        assertEquals(5, cart.items().getFirst().quantity());
    }

    @Test
    public void quantityZeroRemovesTheLine() {
        // Arrange
        AppUser customer = testHelper.customer("remove@test.local");
        testHelper.product("banana", "8.99", 10);
        sut.upsertItem(customer.getId(), "banana", 3);

        // Act
        CartResponse cart = sut.upsertItem(customer.getId(), "banana", 0);

        // Assert
        assertTrue(cart.items().isEmpty());
        assertEquals(BigDecimal.ZERO.setScale(2), cart.totalPrice().setScale(2));
    }

    @Test
    public void anUnknownSlugIsRejected() {
        // Arrange
        AppUser customer = testHelper.customer("unknown@test.local");

        // Act + Assert
        assertThrows(ProductNotFoundException.class,
                () -> sut.upsertItem(customer.getId(), "does-not-exist", 1));
    }

    @Test
    public void aDeactivatedProductCannotBeAddedButCanStillBeRemoved() {
        // Arrange
        AppUser customer = testHelper.customer("withdrawn@test.local");
        Product banana = testHelper.product("banana", "8.99", 10);
        sut.upsertItem(customer.getId(), "banana", 2);
        banana.setActive(false);
        testHelper.deactivate(banana);

        // Act + Assert: adding is refused
        assertThrows(ProductNotAvailableException.class,
                () -> sut.upsertItem(customer.getId(), "banana", 3));

        // but the customer is not stuck with it
        CartResponse cart = sut.upsertItem(customer.getId(), "banana", 0);
        assertTrue(cart.items().isEmpty());
    }

    @Test
    public void cartsAreIsolatedPerCustomer() {
        // Arrange
        AppUser first = testHelper.customer("first@test.local");
        AppUser second = testHelper.customer("second@test.local");
        testHelper.product("banana", "8.99", 10);
        sut.upsertItem(first.getId(), "banana", 4);

        // Act
        CartResponse secondCart = sut.getCart(second.getId());

        // Assert
        assertEquals(1, sut.getCart(first.getId()).items().size());
        assertTrue(secondCart.items().isEmpty());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
