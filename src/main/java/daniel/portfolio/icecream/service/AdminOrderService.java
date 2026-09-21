package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.repository.projection.RevenueSummary;
import daniel.portfolio.icecream.repository.projection.StatusCount;
import daniel.portfolio.icecream.repository.projection.OrderExportProjection;
import daniel.portfolio.icecream.controller.response.AdminOrderResponse;
import daniel.portfolio.icecream.controller.response.OrderAnalyticsResponse;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.repository.OrderItemRepository;
import daniel.portfolio.icecream.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private static final int TOP_PRODUCT_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> findOrders(OrderStatus status, Instant from, Instant to, Pageable pageable) {
        return orderRepository.findForAdmin(status, from, to, pageable)
                .map(AdminOrderResponse::new);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public OrderAnalyticsResponse analytics(Instant from, Instant to) {
        RevenueSummary revenue = orderRepository.sumRevenue(from, to);
        BigDecimal totalRevenue = revenue.getTotalRevenue();
        long revenueOrderCount = revenue.getOrderCount();

        BigDecimal averageOrderValue = revenueOrderCount == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(revenueOrderCount), 2, RoundingMode.HALF_UP);

        Map<OrderStatus, Long> ordersByStatus = orderRepository.countByStatus(from, to).stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getOrderCount));

        List<OrderAnalyticsResponse.TopProduct> topProducts =
                orderItemRepository.findTopProducts(from, to, PageRequest.of(0, TOP_PRODUCT_LIMIT))
                        .stream()
                        .map(row ->
                                new OrderAnalyticsResponse.TopProduct(row.getProductName(), row.getQuantitySold()))
                        .toList();

        return new OrderAnalyticsResponse(
                totalRevenue,
                revenueOrderCount,
                averageOrderValue,
                ordersByStatus,
                topProducts
        );
    }

    @Transactional(readOnly = true)
    public List<OrderExportProjection> findExportPage(
            OrderStatus status, Instant from, Instant to,
            Instant lastCreatedAt, UUID lastItemId, int pageSize) {
        return orderItemRepository.findExportRows(
                status, from, to, lastCreatedAt, lastItemId, PageRequest.ofSize(pageSize));
    }
}
