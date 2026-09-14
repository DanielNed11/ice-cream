package daniel.portfolio.icecream.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "List all products",
        description = "Public endpoint -- no authentication required. Returns every product in the catalog " +
                "with its current price and stock quantity."
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "The full product catalog")
})
public @interface ListProductsApiDocs {
}
