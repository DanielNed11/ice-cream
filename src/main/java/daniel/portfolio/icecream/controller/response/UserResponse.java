package daniel.portfolio.icecream.controller.response;

import daniel.portfolio.icecream.model.Role;

public record UserResponse(
        String name,
        String email,
        Role role
) {
}
