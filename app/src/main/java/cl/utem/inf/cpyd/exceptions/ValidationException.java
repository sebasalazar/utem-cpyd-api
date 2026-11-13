package cl.utem.inf.cpyd.exceptions;

/**
 * Datos de entrada inválidos o que no cumplen una regla de negocio.
 *
 * <p>Se traduce a HTTP 400 mediante {@link cl.utem.inf.cpyd.api.ApiExceptionHandler}.</p>
 */
public class ValidationException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje por defecto ({@code "No fue posible validar su solicitud"}).
     */
    public ValidationException() {
        super("No fue posible validar su solicitud");
    }

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message detalle del error que se devuelve al cliente.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje y la causa original, que se conserva para el
     * seguimiento.
     *
     * @param message detalle del error.
     * @param cause excepción que originó este error.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
