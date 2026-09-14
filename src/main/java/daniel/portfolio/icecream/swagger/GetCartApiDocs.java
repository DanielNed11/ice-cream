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
        summary = "Get the current user's cart",
        description = "Returns the caller's cart contents and total price. If the caller has never added " +
                "anything to their cart, this returns an empty cart rather than a 404 -- no Cart row exists " +
                "yet until the first item is added."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "The caller's current cart"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token")
})
public @interface GetCartApiDocs {
}
