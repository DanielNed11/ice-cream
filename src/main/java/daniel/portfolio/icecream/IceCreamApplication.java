package daniel.portfolio.icecream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IceCreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(IceCreamApplication.class, args);
    }

}
