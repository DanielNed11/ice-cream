package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.TestHelper;
import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.repository.projection.RevenueSummary;
import daniel.portfolio.icecream.repository.projection.StatusCount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository sut;
    @Autowired
    private TestHelper testHelper;
    // A bulk @Modifying query needs a transaction; in production OrderService
    // supplies one. Committing here (rather than annotating the test) also means
    // the assertions below read fresh rows instead of a stale persistence context.
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void transitionStatusOnlyMovesOrdersInTheExpectedStatus() {
        // Arrange
        AppUser customer = testHelper.customer("cas@test.local");
        Order placed = testHelper.order(customer, "CAS00001", OrderStatus.PLACED, "8.99");
        Order cancelled = testHelper.order(customer, "CAS00002", OrderStatus.CANCELLED, "8.99");

        // Act
        int updated = transactionTemplate.execute(tx -> sut.transitionStatus(
                List.of(placed.getId(), cancelled.getId()),
                OrderStatus.PLACED, OrderStatus.DELIVERED, Instant.now()));

        // Assert: the cancelled one is left alone
        assertEquals(1, updated);
        assertEquals(OrderStatus.DELIVERED, sut.findById(placed.getId()).orElseThrow().getStatus());
        assertEquals(OrderStatus.CANCELLED, sut.findById(cancelled.getId()).orElseThrow().getStatus());
    }

    @Test
    public void transitionStatusIsANoOpTheSecondTime() {
        // Arrange
        AppUser customer = testHelper.customer("cas2@test.local");
        Order order = testHelper.order(customer, "CAS00003", OrderStatus.PLACED, "8.99");
        transactionTemplate.execute(tx -> sut.transitionStatus(
                List.of(order.getId()), OrderStatus.PLACED, OrderStatus.CANCELLED, Instant.now()));

        // Act
        int second = transactionTemplate.execute(tx -> sut.transitionStatus(
                List.of(order.getId()), OrderStatus.PLACED, OrderStatus.CANCELLED, Instant.now()));

        // Assert
        assertEquals(0, second);
    }

    @Test
    public void referenceMustBeUnique() {
        // Arrange
        AppUser customer = testHelper.customer("unique@test.local");
        testHelper.order(customer, "DUPL0001", OrderStatus.PLACED, "8.99");

        // Act + Assert
        assertThrows(DataIntegrityViolationException.class,
                () -> testHelper.order(customer, "DUPL0001", OrderStatus.PLACED, "8.99"));
    }

    @Test
    public void findForAdminWithoutFiltersReturnsEverything() {
        // Arrange
        AppUser customer = testHelper.customer("nofilter@test.local");
        testHelper.order(customer, "ADMIN001", OrderStatus.PLACED, "8.99");
        testHelper.order(customer, "ADMIN002", OrderStatus.CANCELLED, "9.49");

        // Act: all three filters null, which is the CAST(:x AS ...) IS NULL path
        Page<Order> all = sut.findForAdmin(null, null, null, PageRequest.of(0, 10));

        // Assert
        assertEquals(2, all.getTotalElements());
    }

    @Test
    public void findForAdminFiltersByStatusAndDateRange() {
        // Arrange
        AppUser customer = testHelper.customer("filtered@test.local");
        testHelper.order(customer, "ADMIN003", OrderStatus.PLACED, "8.99");
        testHelper.order(customer, "ADMIN004", OrderStatus.CANCELLED, "9.49");

        // Act
        Page<Order> onlyPlaced = sut.findForAdmin(OrderStatus.PLACED, null, null, PageRequest.of(0, 10));
        Page<Order> future = sut.findForAdmin(
                null, Instant.now().plus(1, ChronoUnit.DAYS), null, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, onlyPlaced.getTotalElements());
        assertEquals("ADMIN003", onlyPlaced.getContent().getFirst().getReference());
        assertEquals(0, future.getTotalElements());
    }

    @Test
    public void sumRevenueExcludesCancelledOrders() {
        // Arrange
        AppUser customer = testHelper.customer("revenue@test.local");
        testHelper.order(customer, "REV00001", OrderStatus.PLACED, "10.00");
        testHelper.order(customer, "REV00002", OrderStatus.DELIVERED, "20.00");
        testHelper.order(customer, "REV00003", OrderStatus.CANCELLED, "500.00");

        // Act
        RevenueSummary revenue = sut.sumRevenue(null, null);

        // Assert
        assertEquals(0, new BigDecimal("30.00").compareTo(revenue.getTotalRevenue()));
        assertEquals(2L, revenue.getOrderCount());
    }

    @Test
    public void sumRevenueReturnsZeroRatherThanNullWhenNothingMatches() {
        // Act: an empty range, so SUM() has no rows and COALESCE has to supply the 0
        RevenueSummary revenue = sut.sumRevenue(
                Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS));

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(revenue.getTotalRevenue()));
        assertEquals(0L, revenue.getOrderCount());
    }

    @Test
    public void countByStatusGroupsEveryStatus() {
        // Arrange
        AppUser customer = testHelper.customer("counts@test.local");
        testHelper.order(customer, "CNT00001", OrderStatus.PLACED, "8.99");
        testHelper.order(customer, "CNT00002", OrderStatus.PLACED, "8.99");
        testHelper.order(customer, "CNT00003", OrderStatus.CANCELLED, "8.99");

        // Act
        Map<OrderStatus, Long> counts = sut.countByStatus(null, null).stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getOrderCount));

        // Assert
        assertEquals(2L, counts.get(OrderStatus.PLACED));
        assertEquals(1L, counts.get(OrderStatus.CANCELLED));
    }

    @AfterEach
    public void cleanup() {
        testHelper.cleanUp();
    }
}
