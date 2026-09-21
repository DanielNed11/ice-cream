package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.model.SentEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SentEmailRepository extends JpaRepository<SentEmail, UUID> {
}
