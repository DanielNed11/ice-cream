package daniel.portfolio.icecream.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class SentEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 30)
    private EmailType emailType;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EmailStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @PrePersist
    void onCreate() {
        sentAt = Instant.now();
    }

}
