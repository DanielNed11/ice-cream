package daniel.portfolio.icecream.security.jwt;

import daniel.portfolio.icecream.exception.InvalidRefreshTokenException;
import daniel.portfolio.icecream.logging.Sensitive;
import daniel.portfolio.icecream.model.RefreshToken;
import daniel.portfolio.icecream.repository.AppUserRepository;
import daniel.portfolio.icecream.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Sensitive
    public String issue(UUID userId) {
        String rawToken = generateRawToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setAppUser(appUserRepository.getReferenceById(userId));
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Sensitive
    @Transactional
    public RotatedTokens rotate(@Sensitive String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existing);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        String newRawToken = generateRawToken();
        existing.setTokenHash(hash(newRawToken));
        existing.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        refreshTokenRepository.save(existing);

        return new RotatedTokens(newRawToken, existing.getAppUser().getId());
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.deleteAllByAppUserId(userId);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record RotatedTokens(String refreshToken, UUID userId) {
    }
}
