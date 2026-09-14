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
        summary = "Authenticate with email and password",
        description = "Verifies credentials and issues a new access/refresh token pair on success."
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated; access and refresh tokens issued"),
        @ApiResponse(responseCode = "400", description = "Validation failed -- e.g. mixed-case email"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
})
public @interface LoginApiDocs {
}
