package daniel.portfolio.icecream.controller.request;

import daniel.portfolio.icecream.validation.LowercaseEmail;
import daniel.portfolio.icecream.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @LowercaseEmail @Size(max = 100) String email,
        @NotBlank @StrongPassword @Size(min = 8, max = 50) String password
) {
}
