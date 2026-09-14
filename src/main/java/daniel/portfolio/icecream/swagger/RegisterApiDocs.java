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
        summary = "Register a new account",
        description = "Creates a new account with role CUSTOMER (registration never accepts a client-supplied " +
                "role) and immediately issues an access/refresh token pair, matching auto-login behavior."
)
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created; access and refresh tokens issued"),
        @ApiResponse(responseCode = "400", description = "Validation failed -- e.g. weak password, " +
                "mixed-case email, or a field exceeding its length limit"),
        @ApiResponse(responseCode = "409", description = "Email is already registered")
})
public @interface RegisterApiDocs {
}
