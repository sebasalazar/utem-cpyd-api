package cl.utem.inf.cpyd.exceptions;

/**
 * Error de negocio o de configuración que no encaja en otra categoría.
 *
 * <p>Se traduce a HTTP 422 mediante {@link cl.utem.inf.cpyd.api.ApiExceptionHandler}.</p>
 */
public class UtemException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje por defecto ({@code "No se pudo procesar su petición"}).
     */
    public UtemException() {
        super("No se pudo procesar su petición");
    }

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message detalle del error que se devuelve al cliente.
     */
    public UtemException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje y la causa original, que se conserva para el
     * seguimiento.
     *
     * @param message detalle del error.
     * @param cause excepción que originó este error.
     */
    public UtemException(String message, Throwable cause) {
        super(message, cause);
    }

}
