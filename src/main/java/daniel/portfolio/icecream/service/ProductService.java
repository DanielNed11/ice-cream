package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.controller.response.ProductResponse;
import daniel.portfolio.icecream.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> listAll() {
        return productRepository.findAll().stream()
                .map(product -> new ProductResponse(
                        product.getSlug(),
                        product.getName(),
                        product.getPrice(),
                        product.getStockQuantity()
                ))
                .toList();
    }
}
