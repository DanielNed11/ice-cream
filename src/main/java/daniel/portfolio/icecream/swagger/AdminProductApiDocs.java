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
@Operation(summary = "Admin product management (SUPERADMIN only)")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "The affected product"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token"),
        @ApiResponse(responseCode = "403", description = "Caller is not a SUPERADMIN"),
        @ApiResponse(responseCode = "404", description = "No product with that slug"),
        @ApiResponse(responseCode = "409", description = "Slug already belongs to another product")
})
public @interface AdminProductApiDocs {
}
