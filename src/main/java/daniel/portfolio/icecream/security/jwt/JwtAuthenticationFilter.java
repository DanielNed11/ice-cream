package daniel.portfolio.icecream.security.jwt;

import daniel.portfolio.icecream.exception.ErrorResponse;
import daniel.portfolio.icecream.security.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

import static daniel.portfolio.icecream.constants.Constants.SERVICE_UNAVAILABLE;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public static final String JWT_ERROR_ATTRIBUTE = "jwt-error";
    public static final String JWT_ERROR_EXPIRED = "expired";
    public static final String JWT_ERROR_INVALID = "invalid";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (!ObjectUtils.isEmpty(header) && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());

            try {
                UUID userId = jwtService.validateAndGetSubject(token);

                userDetailsService.loadUserById(userId).ifPresent(principal -> {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } catch (ExpiredJwtException ex) {
                log.warn("Expired JWT presented", ex);
                request.setAttribute(JWT_ERROR_ATTRIBUTE, JWT_ERROR_EXPIRED);
            } catch (JwtException | IllegalArgumentException ex) {
                log.warn("Invalid JWT presented", ex);
                request.setAttribute(JWT_ERROR_ATTRIBUTE, JWT_ERROR_INVALID);
            } catch (DataAccessException ex) {
                log.error("Database unavailable while authenticating request", ex);
                writeServiceUnavailable(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeServiceUnavailable(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ErrorResponse body = ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE, request);
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
