package daniel.portfolio.icecream.repository.projection;

import java.math.BigDecimal;

public interface RevenueSummary {

    BigDecimal getTotalRevenue();

    Long getOrderCount();
}
