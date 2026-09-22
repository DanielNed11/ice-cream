package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.repository.projection.OrderExportProjection;
import daniel.portfolio.icecream.repository.projection.TopProductRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository sut;
    @Autowired
    private TestHelper testHelper;

    @Test
    public void findExportRowsPagesThroughEveryRowExactlyOnce() {
        // Arrange
        AppUser customer = testHelper.customer("export@test.local");
        Product banana = testHelper.product("banana", "8.99", 100);
        Order order = testHelper.order(customer, "EXPORT01", OrderStatus.PLACED, "89.90");
        for (int i = 0; i < 10; i++) {
            testHelper.orderItem(order, banana, 1, "8.99");
        }

        // Act: walk the cursor two rows at a time, like the exporter does
        List<UUID> seen = new ArrayList<>();
        Instant lastCreatedAt = null;
        UUID lastItemId = null;
        List<OrderExportProjection> page;

        do {
            page = sut.findExportRows(null, null, null, lastCreatedAt, lastItemId, PageRequest.ofSize(2));
            for (OrderExportProjection row : page) {
                seen.add(row.getItemId());
            }
            if (!page.isEmpty()) {
                OrderExportProjection last = page.getLast();
                lastCreatedAt = last.getPlacedAt();
                lastItemId = last.getItemId();
            }
        } while (page.size() == 2);

        // Assert
        assertEquals(10, seen.size());
        assertEquals(10, seen.stream().distinct().count());
    }

    @Test
    public void findExportRowsAppliesTheStatusFilter() {
        // Arrange
        AppUser customer = testHelper.customer("filter@test.local");
        Product banana = testHelper.product("banana", "8.99", 100);
        Order placed = testHelper.order(customer, "FILTER01", OrderStatus.PLACED, "8.99");
        Order cancelled = testHelper.order(customer, "FILTER02", OrderStatus.CANCELLED, "8.99");
        testHelper.orderItem(placed, banana, 1, "8.99");
        testHelper.orderItem(cancelled, banana, 1, "8.99");

        // Act
        List<OrderExportProjection> onlyCancelled =
                sut.findExportRows(OrderStatus.CANCELLED, null, null, null, null, PageRequest.ofSize(50));

        // Assert
        assertEquals(1, onlyCancelled.size());
        assertEquals("FILTER02", onlyCancelled.getFirst().getReference());
    }

    @Test
    public void findTopProductsExcludesCancelledOrders() {
        // Arrange
        AppUser customer = testHelper.customer("top@test.local");
        Product banana = testHelper.product("banana", "8.99", 100);
        Product chocolate = testHelper.product("chocolate", "9.49", 100);
        Order placed = testHelper.order(customer, "TOP00001", OrderStatus.PLACED, "8.99");
        Order cancelled = testHelper.order(customer, "TOP00002", OrderStatus.CANCELLED, "9.49");
        testHelper.orderItem(placed, banana, 3, "8.99");
        testHelper.orderItem(cancelled, chocolate, 50, "9.49");

        // Act
        List<TopProductRow> topProducts = sut.findTopProducts(null, null, PageRequest.ofSize(5));

        // Assert
        assertEquals(1, topProducts.size());
        assertEquals("BANANA", topProducts.getFirst().getProductName());
        assertEquals(3L, topProducts.getFirst().getQuantitySold());
    }

    @Test
    public void findTopProductsOrdersByUnitsSold() {
        // Arrange
        AppUser customer = testHelper.customer("order@test.local");
        Product banana = testHelper.product("banana", "8.99", 100);
        Product chocolate = testHelper.product("chocolate", "9.49", 100);
        Order order = testHelper.order(customer, "TOP00003", OrderStatus.DELIVERED, "50.00");
        testHelper.orderItem(order, banana, 2, "8.99");
        testHelper.orderItem(order, chocolate, 7, "9.49");

        // Act
        List<TopProductRow> topProducts = sut.findTopProducts(null, null, PageRequest.ofSize(5));

        // Assert
        assertEquals(2, topProducts.size());
        assertEquals("CHOCOLATE", topProducts.getFirst().getProductName());
        assertTrue(topProducts.getFirst().getQuantitySold() > topProducts.getLast().getQuantitySold());
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
