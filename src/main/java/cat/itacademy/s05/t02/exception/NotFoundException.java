package cat.itacademy.s05.t02.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}