package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.exception.ProductNotFoundException;
import daniel.portfolio.icecream.exception.ProductSlugAlreadyExistsException;
import daniel.portfolio.icecream.controller.request.ProductRequest;
import daniel.portfolio.icecream.controller.request.ProductUpdateRequest;
import daniel.portfolio.icecream.controller.response.ProductResponse;
import daniel.portfolio.icecream.model.Product;
import daniel.portfolio.icecream.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static daniel.portfolio.icecream.constants.Constants.PRODUCT_NOT_FOUND;
import static daniel.portfolio.icecream.constants.Constants.PRODUCT_SLUG_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> listActive() {
        return productRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySlug(request.slug())) {
            throw new ProductSlugAlreadyExistsException(PRODUCT_SLUG_ALREADY_EXISTS);
        }

        Product product = new Product();
        apply(product, request);
        product.setActive(true);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(String slug, ProductUpdateRequest request) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException(PRODUCT_NOT_FOUND));

        if (!product.getSlug().equals(request.slug()) && productRepository.existsBySlug(request.slug())) {
            throw new ProductSlugAlreadyExistsException(PRODUCT_SLUG_ALREADY_EXISTS);
        }

        product.setSlug(request.slug());
        product.setName(request.name());
        product.setPrice(request.price());
        return toResponse(product);
    }

    @Transactional
    public ProductResponse setActive(String slug, boolean active) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException(PRODUCT_NOT_FOUND));

        product.setActive(active);
        return toResponse(product);
    }

    private void apply(Product product, ProductRequest request) {
        product.setSlug(request.slug());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
    }

    @Transactional
    public int restockActiveProducts(int amount) {
        return productRepository.restockActiveProducts(amount);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getSlug(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive()
        );
    }
}
