package daniel.portfolio.icecream.event;

import daniel.portfolio.icecream.service.EmailService;
import daniel.portfolio.icecream.service.dto.OrderConfirmationData;
import daniel.portfolio.icecream.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedEventListener {

    private final OrderService orderService;
    private final EmailService emailService;

    @Async(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    @TransactionalEventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        try {
            OrderConfirmationData data = orderService.loadConfirmationData(event.orderId());
            emailService.sendOrderConfirmation(data);
        } catch (Exception ex) {

            log.error("Failed to send order confirmation email for order {}", event.orderId(), ex);
        }
    }
}
