package daniel.portfolio.icecream.controller;

import daniel.portfolio.icecream.constants.ProductApiPaths;
import daniel.portfolio.icecream.controller.response.ProductResponse;
import daniel.portfolio.icecream.service.ProductService;
import daniel.portfolio.icecream.swagger.ListProductsApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ProductApiPaths.PRODUCTS_BASE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @ListProductsApiDocs
    public List<ProductResponse> listProducts() {
        return productService.listAll();
    }
}
