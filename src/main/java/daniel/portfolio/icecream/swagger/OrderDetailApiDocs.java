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
        summary = "Get one of the caller's orders",
        description = "Returns the order with its line items as they were at purchase time. An order " +
                "belonging to another user returns 404 rather than 403, so the endpoint never reveals " +
                "whether that id exists."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "The order"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token"),
        @ApiResponse(responseCode = "404", description = "No such order belonging to the caller")
})
public @interface OrderDetailApiDocs {
}
