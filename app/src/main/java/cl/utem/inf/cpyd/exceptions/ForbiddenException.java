package cl.utem.inf.cpyd.exceptions;

/**
 * Acceso denegado a un recurso protegido, por ejemplo un JWT inválido, expirado, o de una credencial inexistente o inactiva.
 *
 * <p>Se traduce a HTTP 403 mediante {@link cl.utem.inf.cpyd.api.ApiExceptionHandler}.</p>
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje por defecto.
     */
    public ForbiddenException() {
        super("No tiene permitido el acceso a este recurso");
    }

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message detalle del error que se devuelve al cliente.
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje y la causa original, que se conserva para el
     * seguimiento.
     *
     * @param message detalle del error.
     * @param cause excepción que originó este error.
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
