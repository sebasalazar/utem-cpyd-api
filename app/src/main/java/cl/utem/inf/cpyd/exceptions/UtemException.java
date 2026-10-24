package cl.utem.inf.cpyd.exceptions;

public class UtemException extends RuntimeException {

    public UtemException() {
        super("No se pudo precesar su petición");
    }

    public UtemException(String message) {
        super(message);
    }

    public UtemException(String message, Throwable cause) {
        super(message, cause);
    }

}
