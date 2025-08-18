package cat.itacademy.s05.t02.exception;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex, HttpServletRequest req) {
        log.warn("404 Not Found on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(404, "NOT_FOUND", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException ex, HttpServletRequest req) {
        log.warn("409 Conflict on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(409, "CONFLICT", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        log.warn("403 Forbidden on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(403, "FORBIDDEN", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        log.warn("400 Bad Request on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("400 Bad Request on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(400, "BAD_REQUEST", "Malformed JSON or invalid request body", req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("400 Validation failed on {} {} -> {} field errors", req.getMethod(), req.getRequestURI(), fields.size());
        return ValidationErrorResponse.of(400, "VALIDATION_ERROR", "Invalid input data", req.getRequestURI(), fields);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleBind(BindException ex, HttpServletRequest req) {
        List<FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("400 Bind validation failed on {} {} -> {} field errors", req.getMethod(), req.getRequestURI(), fields.size());
        return ValidationErrorResponse.of(400, "VALIDATION_ERROR", "Invalid input data", req.getRequestURI(), fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<FieldError> fields = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        log.warn("400 Constraint violations on {} {} -> {} field errors", req.getMethod(), req.getRequestURI(), fields.size());
        return ValidationErrorResponse.of(400, "VALIDATION_ERROR", "Invalid input data", req.getRequestURI(), fields);
    }

    private FieldError toFieldError(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() != null ? v.getPropertyPath().toString() : null;
        return new FieldError(field, v.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String param = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type";
        String msg = "Parameter '" + param + "' must be a valid " + requiredType;
        log.warn("400 Type mismatch on {} {}: {}", req.getMethod(), req.getRequestURI(), msg);
        return ErrorResponse.of(400, "BAD_REQUEST", msg, req.getRequestURI());
    }


    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(RuntimeException ex, HttpServletRequest req) {
        log.warn("401 Unauthorized on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(401, "UNAUTHORIZED", "Invalid credentials", req.getRequestURI());
    }


    @ExceptionHandler(JWTVerificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwt(JWTVerificationException ex, HttpServletRequest req) {
        log.warn("401 Invalid JWT on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(401, "UNAUTHORIZED", "Invalid JWT token", req.getRequestURI());
    }

    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(Exception ex, HttpServletRequest req) {
        log.warn("403 Forbidden on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(403, "FORBIDDEN", "You do not have permission to perform this action", req.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("409 Conflict on {} {}: {}", req.getMethod(), req.getRequestURI(), msg);
        return ErrorResponse.of(409, "CONFLICT", "Data integrity violation", req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("400 Business rule error on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ErrorResponse.of(400, "BAD_REQUEST", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("500 on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        return ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", "An unexpected server error occurred", req.getRequestURI());
    }
}

