package daniel.portfolio.icecream.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("prod")
public class ResendMailTransport implements MailTransport {

    private static final String SEND_URI = "https://api.resend.com/emails";

    private final RestClient http = RestClient.create();

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.resend.api-key}")
    private String apiKey;

    @Override
    public void send(OutgoingEmail email) {
        Response response = http.post()
                .uri(SEND_URI)
                .headers(headers -> {
                    headers.setBearerAuth(apiKey);
                    String key = idempotencyKey(email);
                    if (key != null) {
                        headers.add("Idempotency-Key", key);
                    }
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", fromName + " <" + from + ">",
                        "to", List.of(email.recipient()),
                        "subject", email.subject(),
                        "html", email.html(),
                        "text", email.text()
                ))
                .retrieve()
                .body(Response.class);

        log.info("Resend accepted the {} email for order {} as {}",
                email.emailType(), email.orderId(), response == null ? "unknown" : response.id());
    }

    private String idempotencyKey(OutgoingEmail email) {
        return email.orderId().toString() +
                email.emailType().toString();
    }

    private record Response(String id) {
    }
}
