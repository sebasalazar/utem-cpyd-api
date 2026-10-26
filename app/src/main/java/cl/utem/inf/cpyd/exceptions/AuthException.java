package cl.utem.inf.cpyd.exceptions;

public class AuthException extends RuntimeException {

    public AuthException() {
        super("Credenciales inválidas");
    }

    public AuthException(String message) {
        super(message);
    }

}
