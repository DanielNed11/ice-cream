package daniel.portfolio.icecream.controller;

import daniel.portfolio.icecream.controller.request.UpsertCartItemRequest;
import daniel.portfolio.icecream.controller.response.CartResponse;
import daniel.portfolio.icecream.security.CustomUser;
import daniel.portfolio.icecream.service.CartService;
import daniel.portfolio.icecream.swagger.GetCartApiDocs;
import daniel.portfolio.icecream.swagger.UpsertCartItemApiDocs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @GetCartApiDocs
    public CartResponse getCart(@AuthenticationPrincipal CustomUser principal) {
        return cartService.getCart(principal.getId());
    }

    @PutMapping("/items")
    @UpsertCartItemApiDocs
    public CartResponse upsertItem(
            @AuthenticationPrincipal CustomUser principal,
            @Valid @RequestBody UpsertCartItemRequest request
    ) {
        return cartService.upsertItem(principal.getId(), request.slug(), request.quantity());
    }
}
