package daniel.portfolio.icecream.controller.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
