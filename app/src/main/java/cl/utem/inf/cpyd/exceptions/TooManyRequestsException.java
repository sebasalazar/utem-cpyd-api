package cl.utem.inf.cpyd.exceptions;

/**
 * Se superó el límite de intentos permitidos.
 *
 * <p>Se traduce a HTTP 429 mediante {@link cl.utem.inf.cpyd.api.ApiExceptionHandler}.</p>
 */
public class TooManyRequestsException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje por defecto ({@code "Demasiados intentos fallidos, intente más tarde"}).
     */
    public TooManyRequestsException() {
        super("Demasiados intentos fallidos, intente más tarde");
    }

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message detalle del error que se devuelve al cliente.
     */
    public TooManyRequestsException(String message) {
        super(message);
    }
}
