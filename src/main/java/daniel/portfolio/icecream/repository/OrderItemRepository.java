package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.model.OrderItem;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.repository.projection.OrderExportProjection;
import daniel.portfolio.icecream.repository.projection.TopProductRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") Collection<UUID> orderIds);

    @Query("""
            SELECT oi.productName AS productName, SUM(oi.quantity) AS quantitySold FROM OrderItem oi
            WHERE oi.order.status <> OrderStatus.CANCELLED
              AND (CAST(:from AS INSTANT) IS NULL OR oi.order.createdAt >= :from)
              AND (CAST(:to AS INSTANT) IS NULL OR oi.order.createdAt < :to)
            GROUP BY oi.productName
            ORDER BY SUM(oi.quantity) DESC, oi.productName
            """)
    List<TopProductRow> findTopProducts(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("""
            SELECT o.reference AS reference,
                   o.createdAt AS placedAt,
                   o.status AS status,
                   o.appUser.email AS customerEmail,
                   oi.productName AS productName,
                   oi.quantity AS quantity,
                   oi.unitPrice AS unitPrice,
                   oi.id AS itemId
            FROM OrderItem oi
            JOIN oi.order o
            WHERE (CAST(:status AS STRING) IS NULL OR o.status = :status)
              AND (CAST(:from AS INSTANT) IS NULL OR o.createdAt >= :from)
              AND (CAST(:to AS INSTANT) IS NULL OR o.createdAt < :to)
              AND (CAST(:lastCreatedAt AS INSTANT) IS NULL
                   OR o.createdAt < :lastCreatedAt
                   OR (o.createdAt = :lastCreatedAt AND oi.id < :lastItemId))
            ORDER BY o.createdAt DESC, oi.id DESC
            """)
    List<OrderExportProjection> findExportRows(
            @Param("status") OrderStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            @Param("lastItemId") UUID lastItemId,
            Pageable pageable
    );
}
