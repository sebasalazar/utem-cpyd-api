package cl.utem.inf.cpyd.exceptions;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("No tiene permitido el acceso a este recurso");
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
