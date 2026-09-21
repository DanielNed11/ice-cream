package daniel.portfolio.icecream.repository.projection;

import daniel.portfolio.icecream.model.OrderStatus;

public interface StatusCount {

    OrderStatus getStatus();

    Long getOrderCount();
}
