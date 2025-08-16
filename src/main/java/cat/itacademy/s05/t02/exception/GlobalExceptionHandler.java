package cat.itacademy.s05.t02.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Malformed JSON, invalid enums, etc.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("400 Bad Request on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(400, "BAD_REQUEST", "Malformed JSON or invalid request body", req.getRequestURI());
    }

    // 400 - @Valid validation on request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("400 Validation failed on {} {} -> {} field errors", req.getMethod(), req.getRequestURI(), fields.size());
        return ValidationErrorResponse.of(400, "VALIDATION_ERROR", "Invalid input data", req.getRequestURI(), fields);
    }

    // 400 - @Validated validation on query/path parameters
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleBind(BindException ex, HttpServletRequest req) {
        List<FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("400 Bind validation failed on {} {} -> {} field errors", req.getMethod(), req.getRequestURI(), fields.size());
        return ValidationErrorResponse.of(400, "VALIDATION_ERROR", "Invalid input data", req.getRequestURI(), fields);
    }

    // 401 - Authentication (credentials)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(RuntimeException ex, HttpServletRequest req) {
        log.warn("401 Unauthorized on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(401, "UNAUTHORIZED", "Invalid credentials", req.getRequestURI());
    }

    // 401 - Invalid JWT
    @ExceptionHandler(JWTVerificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwt(JWTVerificationException ex, HttpServletRequest req) {
        log.warn("401 Invalid JWT on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(401, "UNAUTHORIZED", "Invalid JWT token", req.getRequestURI());
    }

    // 403 - Forbidden (roles/ownership)
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(Exception ex, HttpServletRequest req) {
        log.warn("403 Forbidden on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(403, "FORBIDDEN", "You do not have permission to perform this action", req.getRequestURI());
    }

    // 409 - Integrity/duplicate keys
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("409 Conflict on {} {}: {}", req.getMethod(), req.getRequestURI(), msg);
        return ErrorResponse.of(409, "CONFLICT", "Data integrity violation", req.getRequestURI());
    }

    // 400 - Explicit business rule violations
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("400 Business rule error on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage(), req.getRequestURI());
    }

    // 500 - Generic fallback
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("500 on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        return ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", "An unexpected server error occurred", req.getRequestURI());
    }
}
