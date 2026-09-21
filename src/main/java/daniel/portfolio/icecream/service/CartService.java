package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.exception.ProductNotAvailableException;
import daniel.portfolio.icecream.exception.ProductNotFoundException;
import daniel.portfolio.icecream.controller.response.CartItemResponse;
import daniel.portfolio.icecream.controller.response.CartResponse;
import daniel.portfolio.icecream.model.Cart;
import daniel.portfolio.icecream.model.CartItem;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.repository.AppUserRepository;
import daniel.portfolio.icecream.repository.CartItemRepository;
import daniel.portfolio.icecream.repository.CartRepository;
import daniel.portfolio.icecream.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static daniel.portfolio.icecream.constants.Constants.PRODUCT_NOT_AVAILABLE;
import static daniel.portfolio.icecream.constants.Constants.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    @Transactional
    public CartResponse upsertItem(UUID userId, String slug, int quantity) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException(PRODUCT_NOT_FOUND));

        if (quantity > 0 && !product.isActive()) {
            throw new ProductNotAvailableException(PRODUCT_NOT_AVAILABLE);
        }

        Cart cart = cartRepository.findByAppUserId(userId)
                .orElseGet(() -> createCart(userId));

        Optional<CartItem> existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (quantity == 0) {
            existing.ifPresent(cartItemRepository::delete);
        } else if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCart(userId);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        Optional<Cart> cart = cartRepository.findByAppUserId(userId);

        if (cart.isEmpty()) {
            return new CartResponse(List.of(), BigDecimal.ZERO);
        }

        List<CartItemResponse> items = cartItemRepository.findByCartId(cart.get().getId())
                .stream()
                .map(CartItemResponse::new)
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, totalPrice);
    }

    private Cart createCart(UUID userId) {
        Cart cart = new Cart();
        cart.setAppUser(appUserRepository.getReferenceById(userId));
        return cartRepository.save(cart);
    }
}
