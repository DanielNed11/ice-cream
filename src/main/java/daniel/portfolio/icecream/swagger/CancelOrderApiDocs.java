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
        summary = "Cancel one of the caller's orders",
        description = "Only an order still in PLACED can be cancelled. The status change is a " +
                "compare-and-set, so a repeated or concurrent cancel returns 409 rather than restoring " +
                "stock twice. Cancelling returns every item to stock and sends a cancellation email."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "The cancelled order"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token"),
        @ApiResponse(responseCode = "404", description = "No such order belonging to the caller"),
        @ApiResponse(responseCode = "409", description = "Order is already cancelled or delivered")
})
public @interface CancelOrderApiDocs {
}
