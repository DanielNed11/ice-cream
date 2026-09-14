package daniel.portfolio.icecream.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Add, update, or remove a cart line",
        description = "Sets the given product's line in the caller's cart to exactly the given quantity -- " +
                "this is not an increment. Creates the cart on first use for that user. A quantity of 0 " +
                "removes the line entirely rather than leaving a zero-quantity row. Covers adding a new item, " +
                "a quantity stepper's +/- actions, and removal, all through this one endpoint."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "The caller's cart after applying the change"),
        @ApiResponse(responseCode = "400", description = "Validation failed -- e.g. blank slug or negative quantity"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token"),
        @ApiResponse(responseCode = "404", description = "No product exists with the given slug")
})
public @interface UpsertCartItemApiDocs {
}
