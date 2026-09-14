package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByAppUserId(UUID appUserId);
}
