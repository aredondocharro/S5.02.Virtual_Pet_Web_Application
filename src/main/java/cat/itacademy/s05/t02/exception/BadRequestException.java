package cat.itacademy.s05.t02.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}