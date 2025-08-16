package cat.itacademy.s05.t02.exception;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> errors
) {
    public static ValidationErrorResponse of(int status, String error, String message, String path, List<FieldError> errors) {
        return new ValidationErrorResponse(Instant.now(), status, error, message, path, errors);
    }
}
