package daniel.portfolio.icecream.controller.request;

import daniel.portfolio.icecream.validation.LowercaseEmail;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @LowercaseEmail String email,
        @NotBlank String password
) {
}
