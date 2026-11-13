package cl.utem.inf.cpyd.exceptions;

/**
 * No existen datos para la consulta solicitada.
 *
 * <p>Se traduce a HTTP 404 mediante {@link cl.utem.inf.cpyd.api.ApiExceptionHandler}.</p>
 */
public class NoDataException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje por defecto ({@code "No se han encontrado datos"}).
     */
    public NoDataException() {
        super("No se han encontrado datos");
    }

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message detalle del error que se devuelve al cliente.
     */
    public NoDataException(String message) {
        super(message);
    }

}
