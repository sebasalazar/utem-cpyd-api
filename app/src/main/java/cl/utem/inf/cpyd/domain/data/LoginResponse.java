package cl.utem.inf.cpyd.domain.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * DTO de salida del inicio de sesión. Contiene el JWT que el cliente debe enviar en la
 * cabecera {@code Authorization} de cada petición de datos.
 */
@Schema(description = "Token de acceso emitido tras un inicio de sesión correcto")
public class LoginResponse {

    @Schema(description = "JWT firmado con HS256. Válido 67 minutos. Se envía como `Bearer <jwt>`.",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbHVtbm9AdXRlbS5jbCJ9.firma")
    private String jwt;

    /**
     * Constructor vacío, necesario para la deserialización JSON. Deja el JWT en cadena vacía.
     */
    public LoginResponse() {
        this.jwt = "";
    }

    /**
     * Crea la respuesta con el token.
     *
     * @param jwt token de acceso firmado.
     */
    public LoginResponse(String jwt) {
        this.jwt = jwt;
    }

    /**
     * Devuelve el JWT.
     *
     * @return token de acceso.
     */
    public String getJwt() {
        return jwt;
    }

    /**
     * Asigna el JWT.
     *
     * @param jwt token de acceso.
     */
    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    /**
     * Código hash calculado a partir del JWT.
     *
     * @return código hash.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.jwt);
        return hash;
    }

    /**
     * Dos respuestas son iguales si contienen el mismo JWT.
     *
     * @param obj objeto a comparar.
     * @return {@code true} si el token coincide.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoginResponse other = (LoginResponse) obj;
        return Objects.equals(this.jwt, other.jwt);
    }
}
