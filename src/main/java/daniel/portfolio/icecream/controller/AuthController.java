package daniel.portfolio.icecream.controller;

import daniel.portfolio.icecream.constants.AuthApiPaths;
import daniel.portfolio.icecream.controller.response.AuthResponse;
import daniel.portfolio.icecream.controller.response.UserResponse;
import daniel.portfolio.icecream.service.AppUserService;
import daniel.portfolio.icecream.controller.request.LoginRequest;
import daniel.portfolio.icecream.controller.request.RegisterRequest;
import daniel.portfolio.icecream.security.CustomUser;
import daniel.portfolio.icecream.security.jwt.JwtService;
import daniel.portfolio.icecream.security.jwt.RefreshTokenService;
import daniel.portfolio.icecream.service.UserRegistrationService;
import daniel.portfolio.icecream.swagger.LoginApiDocs;
import daniel.portfolio.icecream.swagger.LogoutApiDocs;
import daniel.portfolio.icecream.swagger.RefreshApiDocs;
import daniel.portfolio.icecream.swagger.MeApiDocs;
import daniel.portfolio.icecream.swagger.RegisterApiDocs;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AuthApiPaths.AUTH_BASE)
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRegistrationService userRegistrationService;
    private final AppUserService appUserService;

    @PostMapping("/register")
    @RegisterApiDocs
    public ResponseEntity<@NonNull AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UUID userId = userRegistrationService.register(request.name(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(issueTokens(userId));
    }

    @PostMapping("/login")
    @LoginApiDocs
    public ResponseEntity<@NonNull AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUser principal = (CustomUser) authentication.getPrincipal();

        return ResponseEntity.ok(issueTokens(principal.getId()));
    }

    @PostMapping("/refresh")
    @RefreshApiDocs
    public ResponseEntity<@NonNull AuthResponse> refresh(@RequestHeader(REFRESH_TOKEN_HEADER) String refreshToken) {
        RefreshTokenService.RotatedTokens rotated = refreshTokenService.rotate(refreshToken);
        String accessToken = jwtService.generateToken(rotated.userId());
        return ResponseEntity.ok(new AuthResponse(accessToken, rotated.refreshToken()));
    }

    @GetMapping("/me")
    @MeApiDocs
    public ResponseEntity<@NonNull UserResponse> me(@AuthenticationPrincipal CustomUser principal) {
        return ResponseEntity.ok(appUserService.findProfile(principal.getId()));
    }

    @PostMapping("/logout")
    @LogoutApiDocs
    public ResponseEntity<@NonNull Void> logout(@AuthenticationPrincipal CustomUser principal) {
        refreshTokenService.revokeAllForUser(principal.getId());
        return ResponseEntity.noContent().build();
    }

    private AuthResponse issueTokens(UUID userId) {
        String accessToken = jwtService.generateToken(userId);
        String refreshToken = refreshTokenService.issue(userId);
        return new AuthResponse(accessToken, refreshToken);
    }
}
