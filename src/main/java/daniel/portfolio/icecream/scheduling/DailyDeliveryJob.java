package daniel.portfolio.icecream.scheduling;

import daniel.portfolio.icecream.service.dto.OrderEmailTarget;
import daniel.portfolio.icecream.service.EmailService;
import daniel.portfolio.icecream.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyDeliveryJob {

    private final OrderService orderService;
    private final EmailService emailService;

    @Scheduled(
            cron = "${app.delivery.cron}",
            zone = "${app.delivery.zone}",
            scheduler = "deliverySchedular"
    )
    @SchedulerLock(
            name = "deliverySchedLock",
            lockAtMostFor = "10m",
            lockAtLeastFor = "1m"
    )
    public void deliverDueOrders() {
        List<OrderEmailTarget> awaitingDelivery = orderService.findOrdersAwaitingDelivery();
        log.info("Daily delivery run: {} order(s) to process", awaitingDelivery.size());

        int delivered = 0;
        int stale = 0;
        int failed = 0;

        for (OrderEmailTarget due : awaitingDelivery) {

            if (!orderService.markOrderDelivered(due.orderId())) {
                stale++;
                log.info("Skipping delivered email for order {}: no longer PLACED", due.orderId());
                continue;
            }

            try {
                emailService.sendOrderDelivered(due.orderId(), due.reference(), due.recipientEmail());
                delivered++;
            } catch (Exception ex) {
                failed++;
                log.error("Order {} was marked DELIVERED but its email failed", due.orderId(), ex);
            }
        }

        log.info("Daily delivery run finished: {} delivered, {} skipped, {} email failures",
                delivered, stale, failed);
    }
}
