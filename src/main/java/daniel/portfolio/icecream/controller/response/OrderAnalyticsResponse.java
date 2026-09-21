package daniel.portfolio.icecream.controller.response;

import daniel.portfolio.icecream.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OrderAnalyticsResponse(
        BigDecimal totalRevenue,
        long revenueOrderCount,
        BigDecimal averageOrderValue,
        Map<OrderStatus, Long> ordersByStatus,
        List<TopProduct> topProducts
) {
    public record TopProduct(String productName, long quantitySold) {
    }
}
