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
        summary = "Log out",
        description = "Revokes every refresh token belonging to the caller -- this signs the account out of " +
                "every session/device, not just the one that made this request. The current access token " +
                "keeps working until it naturally expires; only refreshing is blocked afterward."
)
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "204", description = "All refresh tokens revoked"),
        @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired access token")
})
public @interface LogoutApiDocs {
}
