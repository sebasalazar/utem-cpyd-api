package cl.utem.inf.cpyd.exceptions;

public class ValidationException extends RuntimeException {

    public ValidationException() {
        super("No fue posible validar su solicitud");
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
