package cl.utem.inf.cpyd.domain.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * DTO de entrada para el inicio de sesión.
 *
 * <p>Las anotaciones {@code @Email} y {@code @NotBlank} se verifican porque
 * {@link cl.utem.inf.cpyd.api.v1.LoginRest} recibe el parámetro con {@code @Valid}. Si
 * no se cumplen, la respuesta es HTTP 400.</p>
 */
@Schema(description = "Credenciales para iniciar sesión")
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "Correo registrado y activo. No distingue mayúsculas.",
            example = "alumno@utem.cl", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "Clave asociada al correo. Distingue mayúsculas.",
            example = "mi-api-key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKey;

    /**
     * Devuelve el correo.
     *
     * @return correo.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Asigna el correo.
     *
     * @param email correo.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Devuelve la API key.
     *
     * @return clave.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Asigna la API key.
     *
     * @param apiKey clave.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Código hash calculado solo con el correo, coherente con {@link #equals(Object)}.
     *
     * @return código hash.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.email);
        return hash;
    }

    /**
     * Dos solicitudes son iguales si tienen el mismo correo. La API key no interviene.
     *
     * @param obj objeto a comparar.
     * @return {@code true} si el correo coincide.
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
        final LoginRequest other = (LoginRequest) obj;
        return Objects.equals(this.email, other.email);
    }
}
