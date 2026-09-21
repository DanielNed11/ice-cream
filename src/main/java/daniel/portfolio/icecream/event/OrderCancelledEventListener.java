package daniel.portfolio.icecream.event;

import daniel.portfolio.icecream.service.EmailService;
import daniel.portfolio.icecream.service.OrderService;
import daniel.portfolio.icecream.service.dto.OrderEmailTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledEventListener {

    private final OrderService orderService;
    private final EmailService emailService;

    @Async(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    @TransactionalEventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        try {
            OrderEmailTarget target = orderService.loadEmailTarget(event.orderId());
            emailService.sendOrderCancelled(target.orderId(), target.reference(), target.recipientEmail());
        } catch (Exception ex) {
            log.error("Failed to send order cancellation email for order {}", event.orderId(), ex);
        }
    }
}
