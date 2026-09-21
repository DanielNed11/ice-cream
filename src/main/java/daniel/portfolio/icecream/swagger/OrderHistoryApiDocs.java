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
        summary = "List the caller's orders",
        description = "Paginated, newest first. Only the authenticated user's own orders are returned; " +
                "supports the usual page, size and sort query parameters."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "A page of the caller's orders"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token")
})
public @interface OrderHistoryApiDocs {
}
