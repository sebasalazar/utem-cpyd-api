package cl.utem.inf.cpyd.exceptions;

public class NoDataException extends RuntimeException {

    public NoDataException() {
        super("No se han encontrado datos");
    }

    public NoDataException(String message) {
        super(message);
    }

}
