package daniel.portfolio.icecream.security;

import daniel.portfolio.icecream.exception.ErrorResponse;
import daniel.portfolio.icecream.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static daniel.portfolio.icecream.constants.Constants.AUTHENTICATION_REQUIRED;
import static daniel.portfolio.icecream.constants.Constants.TOKEN_EXPIRED;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String reason = (String) request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);
        String message = JwtAuthenticationFilter.JWT_ERROR_EXPIRED.equals(reason)
                ? TOKEN_EXPIRED
                : AUTHENTICATION_REQUIRED;

        ErrorResponse body = ErrorResponse.of(HttpStatus.UNAUTHORIZED, message, request);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
