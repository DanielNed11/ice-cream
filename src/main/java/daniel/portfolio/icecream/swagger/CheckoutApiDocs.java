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
        summary = "Check out the current cart",
        description = "Converts the caller's cart into an order: atomically decrements stock for every line " +
                "item (all-or-nothing -- if any item lacks sufficient stock, nothing is decremented and no " +
                "order is created), snapshots each item's name/price at time of purchase, clears the cart, " +
                "and sends an order-confirmation email. The email is sent best-effort after the order is " +
                "already committed -- a mail failure never affects whether the order succeeds."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order placed"),
        @ApiResponse(responseCode = "400", description = "Cart is empty"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token"),
        @ApiResponse(responseCode = "409", description = "One or more items in the cart no longer have enough stock")
})
public @interface CheckoutApiDocs {
}
