package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.exception.EmptyCartException;
import daniel.portfolio.icecream.exception.InsufficientStockException;
import daniel.portfolio.icecream.exception.OrderNotCancellableException;
import daniel.portfolio.icecream.exception.OrderNotFoundException;
import daniel.portfolio.icecream.exception.ProductNotAvailableException;
import daniel.portfolio.icecream.service.dto.OrderEmailTarget;
import daniel.portfolio.icecream.service.dto.OrderConfirmationData;
import daniel.portfolio.icecream.controller.response.OrderItemResponse;
import daniel.portfolio.icecream.controller.response.OrderResponse;
import daniel.portfolio.icecream.event.OrderCancelledEvent;
import daniel.portfolio.icecream.event.OrderPlacedEvent;
import daniel.portfolio.icecream.model.Cart;
import daniel.portfolio.icecream.model.CartItem;
import daniel.portfolio.icecream.model.Order;
import daniel.portfolio.icecream.model.OrderItem;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.repository.AppUserRepository;
import daniel.portfolio.icecream.repository.CartItemRepository;
import daniel.portfolio.icecream.repository.CartRepository;
import daniel.portfolio.icecream.repository.OrderItemRepository;
import daniel.portfolio.icecream.repository.OrderRepository;
import daniel.portfolio.icecream.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static daniel.portfolio.icecream.constants.Constants.CART_IS_EMPTY;
import static daniel.portfolio.icecream.constants.Constants.INSUFFICIENT_STOCK;
import static daniel.portfolio.icecream.constants.Constants.ORDER_NOT_CANCELLABLE;
import static daniel.portfolio.icecream.constants.Constants.ORDER_NOT_FOUND;
import static daniel.portfolio.icecream.constants.Constants.PRODUCT_NOT_AVAILABLE;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int REFERENCE_LENGTH = 8;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse checkout(UUID userId) {
        Cart cart = cartRepository.findByAppUserIdForUpdate(userId)
                .orElseThrow(() -> new EmptyCartException(CART_IS_EMPTY));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .sorted(Comparator.comparing(cartItem -> cartItem.getProduct().getId()))
                .toList();
        if (cartItems.isEmpty()) {
            throw new EmptyCartException(CART_IS_EMPTY);
        }

        Order order = new Order();
        order.setReference(nextAvailableReference());
        order.setAppUser(appUserRepository.getReferenceById(userId));
        order.setStatus(OrderStatus.PLACED);
        order.setTotalPrice(BigDecimal.ZERO);
        orderRepository.save(order);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            reserveStock(cartItem);

            OrderItem orderItem = toOrderItem(order, cartItem);
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(
                    orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        order.setTotalPrice(totalPrice);

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(cartItems);
        eventPublisher.publishEvent(new OrderPlacedEvent(order.getId()));

        return toResponse(order, orderItems);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findOrders(UUID userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByAppUserId(userId, pageable);
        if (orders.isEmpty()) {
            return orders.map(order -> toResponse(order, List.of()));
        }

        List<UUID> orderIds = orders.map(Order::getId).toList();
        Map<UUID, List<OrderItem>> itemsByOrder = orderItemRepository.findByOrderIdIn(orderIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        return orders.map(order -> toResponse(order, itemsByOrder.getOrDefault(order.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrder(UUID userId, String reference) {
        Order order = orderRepository.findByReferenceAndAppUserId(reference, userId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    @Transactional
    public OrderResponse cancel(UUID userId, String reference) {
        Order order = orderRepository.findByReferenceAndAppUserId(reference, userId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));
        UUID orderId = order.getId();

        int cancelled = orderRepository.transitionStatus(
                List.of(orderId), OrderStatus.PLACED, OrderStatus.CANCELLED, Instant.now());
        if (cancelled == 0) {
            throw new OrderNotCancellableException(ORDER_NOT_CANCELLABLE);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.stream()
                .sorted(Comparator.comparing(orderItem -> orderItem.getProduct().getId()))
                .forEach(orderItem ->
                        productRepository.restoreStock(orderItem.getProduct().getId(), orderItem.getQuantity()));

        eventPublisher.publishEvent(new OrderCancelledEvent(orderId));

        return toResponse(order, orderItems, OrderStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public OrderEmailTarget loadEmailTarget(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND));

        return new OrderEmailTarget(orderId, order.getReference(), order.getAppUser().getEmail());
    }

    private void reserveStock(CartItem cartItem) {
        if (!cartItem.getProduct().isActive()) {
            throw new ProductNotAvailableException(PRODUCT_NOT_AVAILABLE);
        }

        int updatedRows = productRepository.decrementStock(
                cartItem.getProduct().getId(), cartItem.getQuantity());

        if (updatedRows == 0) {
            throw new InsufficientStockException(INSUFFICIENT_STOCK);
        }
    }

    // Name and price are copied, not referenced: an order line must keep what
    // the customer actually paid even after the product is edited.
    private OrderItem toOrderItem(Order order, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setProductName(cartItem.getProduct().getName());
        orderItem.setUnitPrice(cartItem.getProduct().getPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        return orderItem;
    }

    private String nextAvailableReference() {
        String hex = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        for (int start = 0; start + REFERENCE_LENGTH <= hex.length(); start += REFERENCE_LENGTH) {
            String candidate = hex.substring(start, start + REFERENCE_LENGTH);
            if (!orderRepository.existsByReference(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Could not allocate a unique order reference");
    }

    private OrderResponse toResponse(Order order, List<OrderItem> orderItems) {
        return toResponse(order, orderItems, order.getStatus());
    }

    private OrderResponse toResponse(Order order, List<OrderItem> orderItems, OrderStatus status) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getSlug(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getReference(),
                status,
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    @Transactional(readOnly = true)
    public OrderConfirmationData loadConfirmationData(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        List<OrderConfirmationData.LineItem> lineItems = orderItemRepository
                .findByOrderId(orderId)
                .stream()
                .map(item -> new OrderConfirmationData.LineItem(
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity())
                )
                .toList();

        return new OrderConfirmationData(
                order.getId(),
                order.getReference(),
                order.getAppUser().getEmail(),
                order.getTotalPrice(),
                lineItems
        );
    }

    @Transactional(readOnly = true)
    public List<OrderEmailTarget> findOrdersAwaitingDelivery() {
        return orderRepository.findByStatus(OrderStatus.PLACED)
                .stream()
                .map(order -> new OrderEmailTarget(
                        order.getId(),
                        order.getReference(),
                        order.getAppUser().getEmail()
                ))
                .toList();
    }

    @Transactional
    public boolean markOrderDelivered(UUID orderId) {
        int updated = orderRepository.transitionStatus(
                List.of(orderId), OrderStatus.PLACED, OrderStatus.DELIVERED, Instant.now());
        return updated == 1;
    }
}
