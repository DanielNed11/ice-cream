package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.model.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByAppUserId(UUID appUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.appUser.id = :appUserId")
    Optional<Cart> findByAppUserIdForUpdate(@Param("appUserId") UUID appUserId);
}
