package cl.utem.inf.cpyd.domain.repository;

import cl.utem.inf.cpyd.domain.model.Credential;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio para la gestión de entidades Credential. Extiende JpaRepository
 * para operaciones CRUD estándar y define consultas específicas para el dominio
 * de autenticación.
 */
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Busca una credencial utilizando su token de acceso o recuperación.
     *
     * @param token El identificador único de tipo UUID.
     * @return Un Optional conteniendo la credencial si existe, o vacío si no.
     */
    Optional<Credential> findByToken(UUID token);

    /**
     * Busca una credencial por correo electrónico, ignorando
     * mayúsculas/minúsculas.
     *
     * @param email La dirección de correo electrónico a buscar.
     * @return Un Optional conteniendo la credencial si existe, o vacío si no.
     */
    Optional<Credential> findByEmailIgnoreCase(String email);

    /**
     * Recupera una lista de correos electrónicos en minúsculas que se
     * encuentran en estado activo/válido.
     *
     * @return Lista de cadenas de texto representando los correos válidos.
     */
    @Query("SELECT LOWER(c.email) FROM Credential c WHERE c.active = true")
    List<String> findActiveEmailsInLowerCase();
}
