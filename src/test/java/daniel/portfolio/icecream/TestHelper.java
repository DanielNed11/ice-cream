package daniel.portfolio.icecream;

import daniel.portfolio.icecream.model.AppUser;
import daniel.portfolio.icecream.model.Cart;
import daniel.portfolio.icecream.model.CartItem;
import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderItem;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.model.Role;
import daniel.portfolio.icecream.repository.AppUserRepository;
import daniel.portfolio.icecream.repository.CartItemRepository;
import daniel.portfolio.icecream.repository.CartRepository;
import daniel.portfolio.icecream.repository.OrderItemRepository;
import daniel.portfolio.icecream.repository.OrderRepository;
import daniel.portfolio.icecream.repository.ProductRepository;
import daniel.portfolio.icecream.repository.RefreshTokenRepository;
import daniel.portfolio.icecream.repository.SentEmailRepository;
import daniel.portfolio.icecream.security.jwt.JwtService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TestHelper {

    public static final String PASSWORD = "Str0ngPass!23";

    private final AppUserRepository appUserRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SentEmailRepository sentEmailRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public TestHelper(AppUserRepository appUserRepository,
                      ProductRepository productRepository,
                      CartRepository cartRepository,
                      CartItemRepository cartItemRepository,
                      OrderRepository orderRepository,
                      OrderItemRepository orderItemRepository,
                      SentEmailRepository sentEmailRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.sentEmailRepository = sentEmailRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public AppUser customer(String email) {
        return user(email, Role.CUSTOMER);
    }

    public AppUser admin(String email) {
        return user(email, Role.ADMIN);
    }

    public AppUser superAdmin(String email) {
        return user(email, Role.SUPERADMIN);
    }

    private AppUser user(String email, Role role) {
        AppUser appUser = new AppUser();
        appUser.setName("Test " + role);
        appUser.setEmail(email);
        appUser.setPassword(PASSWORD);
        appUser.setRole(role);
        return appUserRepository.save(appUser);
    }

    // A real signed access token, so security tests exercise the actual
    // JwtAuthenticationFilter rather than a mocked SecurityContext.
    public String token(AppUser appUser) {
        return jwtService.generateToken(appUser.getId());
    }

    public Product product(String slug, String price, int stockQuantity) {
        return product(slug, price, stockQuantity, true);
    }

    public Product product(String slug, String price, int stockQuantity, boolean active) {
        Product product = new Product();
        product.setSlug(slug);
        product.setName(slug.toUpperCase());
        product.setPrice(new BigDecimal(price));
        product.setStockQuantity(stockQuantity);
        product.setActive(active);
        return productRepository.save(product);
    }

    public Product deactivate(Product product) {
        product.setActive(false);
        return productRepository.save(product);
    }

    public Cart cart(AppUser appUser) {
        Cart cart = new Cart();
        cart.setAppUser(appUser);
        return cartRepository.save(cart);
    }

    public CartItem cartItem(Cart cart, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public Order order(AppUser appUser, String reference, OrderStatus status, String totalPrice) {
        Order order = new Order();
        order.setAppUser(appUser);
        order.setReference(reference);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal(totalPrice));
        return orderRepository.save(order);
    }

    public OrderItem orderItem(Order order, Product product, int quantity, String unitPrice) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setProductName(product.getName());
        orderItem.setUnitPrice(new BigDecimal(unitPrice));
        orderItem.setQuantity(quantity);
        return orderItemRepository.save(orderItem);
    }

    // Deleted child first: every table below is referenced by the one above it.
    public void cleanUp() {
        sentEmailRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        productRepository.deleteAll();
        appUserRepository.deleteAll();
    }
}
