package daniel.portfolio.icecream.controller;

import daniel.portfolio.icecream.controller.response.OrderResponse;
import daniel.portfolio.icecream.security.CustomUser;
import daniel.portfolio.icecream.service.OrderService;
import daniel.portfolio.icecream.swagger.CancelOrderApiDocs;
import daniel.portfolio.icecream.swagger.CheckoutApiDocs;
import daniel.portfolio.icecream.swagger.OrderDetailApiDocs;
import daniel.portfolio.icecream.swagger.OrderHistoryApiDocs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @CheckoutApiDocs
    public ResponseEntity<@NonNull OrderResponse> checkout(@AuthenticationPrincipal CustomUser principal) {
        OrderResponse order = orderService.checkout(principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    @OrderHistoryApiDocs
    public ResponseEntity<@NonNull PagedModel<@NonNull OrderResponse>> listOrders(
            @AuthenticationPrincipal CustomUser principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponse> orders = orderService.findOrders(principal.getId(), pageable);
        return ResponseEntity.ok(new PagedModel<>(orders));
    }

    @PostMapping("/{reference}/cancel")
    @CancelOrderApiDocs
    public ResponseEntity<@NonNull OrderResponse> cancelOrder(
            @AuthenticationPrincipal CustomUser principal,
            @PathVariable String reference
    ) {
        return ResponseEntity.ok(orderService.cancel(principal.getId(), reference));
    }

    @GetMapping("/{reference}")
    @OrderDetailApiDocs
    public ResponseEntity<@NonNull OrderResponse> getOrder(
            @AuthenticationPrincipal CustomUser principal,
            @PathVariable String reference
    ) {
        return ResponseEntity.ok(orderService.findOrder(principal.getId(), reference));
    }
}
