package daniel.portfolio.icecream.scheduling;

import daniel.portfolio.icecream.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyRestockJob {

    private final ProductService productService;

    @Value("${app.restock.amount}")
    private int amount;

    @Scheduled(
            cron = "${app.restock.cron}",
            zone = "${app.restock.zone}",
            scheduler = "deliverySchedular"
    )
    @SchedulerLock(
            name = "restockSchedLock",
            lockAtMostFor = "5m",
            lockAtLeastFor = "1m"
    )
    public void restock() {
        int restocked = productService.restockActiveProducts(amount);
        log.info("Daily restock: +{} units on {} active product(s)", amount, restocked);
    }
}
