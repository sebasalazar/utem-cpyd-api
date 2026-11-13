package cl.utem.inf.cpyd.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

/**
 * Entidad JPA que representa una credencial de acceso, mapeada a la tabla
 * {@code credentials}.
 *
 * <p>Cada fila permite que un correo obtenga un JWT mediante su API key. El campo
 * {@code token} es un identificador interno (UUID) que viaja dentro del JWT y permite
 * localizar la credencial sin exponer el correo ni la clave. El campo {@code active}
 * permite desactivar a un usuario sin borrar su registro.</p>
 */
@Entity
@Table(name = "credentials")
public class Credential extends PkEntityBase {

    /** Identificador interno de la credencial (UUID), incluido en el JWT como {@code tokenId}. */
    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    /** Correo del usuario; único sin distinguir mayúsculas. */
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Clave secreta del usuario, almacenada en texto plano (ver {@code SEGURIDAD.md}). */
    @NotEmpty
    @Column(name = "api_key", nullable = false)
    private String apiKey;

    /** {@code true} si el usuario puede autenticarse y usar su JWT. */
    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Devuelve el token interno de la credencial.
     *
     * @return identificador UUID.
     */
    public UUID getToken() {
        return token;
    }

    /**
     * Asigna el token interno.
     *
     * @param token identificador UUID.
     */
    public void setToken(UUID token) {
        this.token = token;
    }

    /**
     * Devuelve el correo del usuario.
     *
     * @return correo.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Asigna el correo del usuario.
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
     * Indica si la credencial puede usarse para autenticarse.
     *
     * @return {@code true} si está activa.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Activa o desactiva la credencial.
     *
     * @param active {@code true} para activarla.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
