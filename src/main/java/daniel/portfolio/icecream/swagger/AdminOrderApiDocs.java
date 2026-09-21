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
@SecurityRequirement(name = "bearerAuth")
@Operation(summary = "Admin order reporting (ADMIN or SUPERADMIN)")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Requested report or page"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token"),
        @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN or SUPERADMIN")
})
public @interface AdminOrderApiDocs {
}
