package daniel.portfolio.icecream.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Rotate a refresh token",
        description = "Exchanges a valid refresh token for a new access/refresh token pair. The refresh token " +
                "is single-use: the presented token is invalidated as part of the rotation, and reusing an " +
                "already-rotated token fails the same way an unrecognized one does."
)
@Parameter(
        name = "X-Refresh-Token",
        in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
        required = true,
        description = "The refresh token issued at registration, login, or the previous rotation"
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rotated; new access and refresh tokens issued"),
        @ApiResponse(responseCode = "400", description = "X-Refresh-Token header is missing or empty"),
        @ApiResponse(responseCode = "401", description = "Refresh token not recognized, already rotated, or expired")
})
public @interface RefreshApiDocs {
}
