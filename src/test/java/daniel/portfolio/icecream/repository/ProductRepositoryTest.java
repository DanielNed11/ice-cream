package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository sut;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void decrementStockSucceedsWhenThereIsEnough() {
        // Arrange
        Product banana = testHelper.product("banana", "8.99", 10);

        // Act
        int updated = transactionTemplate.execute(tx -> sut.decrementStock(banana.getId(), 4));

        // Assert
        assertEquals(1, updated);
        assertEquals(6, sut.findById(banana.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    public void decrementStockRefusesToGoNegativeAndChangesNothing() {
        // Arrange
        Product banana = testHelper.product("banana", "8.99", 3);

        // Act
        int updated = transactionTemplate.execute(tx -> sut.decrementStock(banana.getId(), 5));

        // Assert: the conditional UPDATE matched no rows, so stock is untouched
        assertEquals(0, updated);
        assertEquals(3, sut.findById(banana.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    public void decrementStockAllowsTakingExactlyTheLastUnits() {
        // Arrange
        Product banana = testHelper.product("banana", "8.99", 3);

        // Act
        int updated = transactionTemplate.execute(tx -> sut.decrementStock(banana.getId(), 3));

        // Assert
        assertEquals(1, updated);
        assertEquals(0, sut.findById(banana.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    public void restoreStockAddsTheUnitsBack() {
        // Arrange
        Product banana = testHelper.product("banana", "8.99", 5);

        // Act
        transactionTemplate.execute(tx -> sut.restoreStock(banana.getId(), 4));

        // Assert
        assertEquals(9, sut.findById(banana.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    public void restockOnlyToppedUpActiveProducts() {
        // Arrange
        Product active = testHelper.product("banana", "8.99", 5);
        Product withdrawn = testHelper.product("pistachio", "9.49", 5, false);

        // Act
        int restocked = transactionTemplate.execute(tx -> sut.restockActiveProducts(10));

        // Assert
        assertEquals(1, restocked);
        assertEquals(15, sut.findById(active.getId()).orElseThrow().getStockQuantity());
        assertEquals(5, sut.findById(withdrawn.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    public void findByActiveTrueHidesWithdrawnProducts() {
        // Arrange
        testHelper.product("banana", "8.99", 5);
        testHelper.product("pistachio", "9.49", 5, false);

        // Act
        List<Product> active = sut.findByActiveTrue();

        // Assert
        assertEquals(1, active.size());
        assertEquals("banana", active.getFirst().getSlug());
        assertTrue(sut.findBySlug("pistachio").isPresent());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
