package cl.utem.inf.cpyd.exceptions;

/**
 * Error de autenticación: credenciales incorrectas o no reconocidas.
 *
 * <p>Se traduce a HTTP 401 mediante {@link cl.utem.inf.cpyd.api.ApiExceptionHandler}.</p>
 */
public class AuthException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje por defecto ({@code "Credenciales inválidas"}).
     */
    public AuthException() {
        super("Credenciales inválidas");
    }

    /**
     * Crea la excepción con un mensaje descriptivo.
     *
     * @param message detalle del error que se devuelve al cliente.
     */
    public AuthException(String message) {
        super(message);
    }

}
