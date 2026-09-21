package daniel.portfolio.icecream.controller;

import daniel.portfolio.icecream.constants.ProductApiPaths;
import daniel.portfolio.icecream.controller.request.ProductRequest;
import daniel.portfolio.icecream.controller.request.ProductUpdateRequest;
import daniel.portfolio.icecream.controller.response.ProductResponse;
import daniel.portfolio.icecream.service.ProductService;
import daniel.portfolio.icecream.swagger.AdminProductApiDocs;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ProductApiPaths.ADMIN_PRODUCTS_BASE)
@PreAuthorize("hasRole('SUPERADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    @AdminProductApiDocs
    public ResponseEntity<@NonNull List<ProductResponse>> listAll() {
        return ResponseEntity.ok(productService.listAll());
    }

    @PostMapping
    @AdminProductApiDocs
    public ResponseEntity<@NonNull ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{slug}")
    @AdminProductApiDocs
    public ResponseEntity<@NonNull ProductResponse> update(@PathVariable String slug, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(slug, request));
    }

    @DeleteMapping("/{slug}")
    @AdminProductApiDocs
    public ResponseEntity<@NonNull ProductResponse> deactivate(@PathVariable String slug) {
        return ResponseEntity.ok(productService.setActive(slug, false));
    }

    @PostMapping("/{slug}/activate")
    @AdminProductApiDocs
    public ResponseEntity<@NonNull ProductResponse> activate(@PathVariable String slug) {
        return ResponseEntity.ok(productService.setActive(slug, true));
    }
}
