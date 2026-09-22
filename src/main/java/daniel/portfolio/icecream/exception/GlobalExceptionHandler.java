package daniel.portfolio.icecream.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.hibernate.query.sqm.PathElementException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

import static daniel.portfolio.icecream.constants.Constants.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, INVALID_EMAIL_OR_PASSWORD, request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_TOKEN, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, ACCESS_DENIED, request);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, EMAIL_ALREADY_REGISTERED, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json", request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Required header '" + ex.getHeaderName() + "' is missing", request);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, PRODUCT_NOT_FOUND, request);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleEmptyCart(
            EmptyCartException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, CART_IS_EMPTY, request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, INSUFFICIENT_STOCK, request);
    }

    @ExceptionHandler(ProductNotAvailableException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleProductNotAvailable(
            ProductNotAvailableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, PRODUCT_NOT_AVAILABLE, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, USER_NOT_FOUND, request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND, request);
    }

    @ExceptionHandler(OrderNotCancellableException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleOrderNotCancellable(
            OrderNotCancellableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, ORDER_NOT_CANCELLABLE, request);
    }

    @ExceptionHandler(ProductSlugAlreadyExistsException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleProductSlugExists(
            ProductSlugAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, PRODUCT_SLUG_ALREADY_EXISTS, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Request method '" + ex.getMethod() + "' is not supported for this endpoint", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        Class<?> requiredType = ex.getRequiredType();

        String expected = requiredType != null && requiredType.isEnum()
                ? "one of: " + Arrays.stream(requiredType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
                : "a valid " + (requiredType == null ? "value" : requiredType.getSimpleName());

        return build(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "': expected " + expected,
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "No endpoint " + request.getMethod() + " " + request.getRequestURI(),
                request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUnknownSortProperty(
            PropertyReferenceException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Cannot sort by unknown property '" + ex.getPropertyName() + "'", request);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInvalidDataAccessUsage(
            InvalidDataAccessApiUsageException ex,
            HttpServletRequest request
    ) {
        if (NestedExceptionUtils.getMostSpecificCause(ex) instanceof PathElementException) {
            return build(HttpStatus.BAD_REQUEST, "Cannot sort by an unknown property", request);
        }

        log.error("Invalid data access usage on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_OCCURRED, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUnknown(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_OCCURRED, request);
    }

    private ResponseEntity<@NonNull ErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message, request));
    }
}
