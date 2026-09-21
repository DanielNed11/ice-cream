package daniel.portfolio.icecream.repository;

import daniel.portfolio.icecream.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);

    List<Product> findByActiveTrue();

    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity " +
            "WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int decrementStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :amount WHERE p.active = true")
    int restockActiveProducts(@Param("amount") int amount);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :productId")
    int restoreStock(@Param("productId") UUID productId, @Param("quantity") int quantity);
}
