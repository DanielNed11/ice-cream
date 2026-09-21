package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.repository.projection.RevenueSummary;
import daniel.portfolio.icecream.repository.projection.StatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatus(OrderStatus status);

    boolean existsByReference(String reference);

    Optional<Order> findByReferenceAndAppUserId(String reference, UUID appUserId);

    Page<Order> findByAppUserId(UUID appUserId, Pageable pageable);

    Optional<Order> findByIdAndAppUserId(UUID id, UUID appUserId);

    @Query("""
            SELECT o FROM Order o JOIN FETCH o.appUser
            WHERE (CAST(:status AS STRING) IS NULL OR o.status = :status)
              AND (CAST(:from AS INSTANT) IS NULL OR o.createdAt >= :from)
              AND (CAST(:to AS INSTANT) IS NULL OR o.createdAt < :to)
            """)
    Page<Order> findForAdmin(
            @Param("status") OrderStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(o.totalPrice), 0) AS totalRevenue, COUNT(o) AS orderCount FROM Order o
            WHERE o.status <> OrderStatus.CANCELLED
              AND (CAST(:from AS INSTANT) IS NULL OR o.createdAt >= :from)
              AND (CAST(:to AS INSTANT) IS NULL OR o.createdAt < :to)
            """)
    RevenueSummary sumRevenue(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT o.status AS status, COUNT(o) AS orderCount FROM Order o
            WHERE (CAST(:from AS INSTANT) IS NULL OR o.createdAt >= :from)
              AND (CAST(:to AS INSTANT) IS NULL OR o.createdAt < :to)
            GROUP BY o.status
            """)
    List<StatusCount> countByStatus(@Param("from") Instant from, @Param("to") Instant to);

    @Modifying
    @Query("UPDATE Order o SET o.status = :to, o.updatedAt = :updatedAt " +
            "WHERE o.id IN :orderIds AND o.status = :from")
    int transitionStatus(
            @Param("orderIds") List<UUID> orderIds,
            @Param("from") OrderStatus from,
            @Param("to") OrderStatus to,
            @Param("updatedAt") Instant updatedAt
    );
}
