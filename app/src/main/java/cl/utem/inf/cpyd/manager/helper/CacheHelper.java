package cl.utem.inf.cpyd.manager.helper;

import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.domain.repository.CredentialRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Auxiliar que encapsula el acceso cacheado a datos de credenciales y correos
 * activos.
 *
 * <p>
 * Las anotaciones de caché se colocan en esta clase y no en los managers de
 * negocio, porque Spring aplica el proxy de caché solo a llamadas que cruzan la
 * frontera del bean. Una llamada interna (self-invocation) dentro del mismo
 * servicio omitiría la interceptación de la caché, provocando consultas
 * innecesarias a la base de datos.</p>
 *
 * <h2>Glosario de Términos</h2>
 * <dl>
 * <dt>Caché (Cache)</dt>
 * <dd>Capa de almacenamiento temporal de alta velocidad que guarda resultados
 * de operaciones costosas para evitar recalcularlos o consultar la base de
 * datos en cada petición.</dd>
 * <dt>Invalidación (Eviction)</dt>
 * <dd>Proceso de eliminación deliberada de datos en la caché para forzar que la
 * próxima consulta obtenga la información actualizada desde la fuente original
 * (base de datos).</dd>
 * <dt>Cache Stampede (Thundering Herd)</dt>
 * <dd>Fenómeno donde múltiples hilos solicitan simultáneamente un dato que
 * acaba de expirar en la caché, generando una sobrecarga repentina en la base
 * de datos. Se mitiga con {@code sync = true}.</dd>
 * </dl>
 *
 * @author Sebastián Salazar Molina
 * @email ssalazar@utem.cl
 * @see <a href="https://orcid.org/0009-0006-1498-5658">ORCID:
 * 0009-0006-1498-5658</a>
 * @institution Universidad Tecnológica Metropolitana (UTEM)
 * @version 0.0.2-SNAPSHOT
 * @since 0.0.1
 */
@Service
public class CacheHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheHelper.class);

    private final CredentialRepository credentialRepository;

    // Nombres de caché explícitos para facilitar monitoreo y administración
    private static final String CACHE_ACTIVE_EMAILS = "active-emails";
    private static final String CACHE_CREDENTIAL_BY_TOKEN = "credential-by-token";
    private static final String CACHE_CREDENTIAL_BY_EMAIL = "credential-by-email";

    public CacheHelper(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    /**
     * Devuelve el conjunto de correos electrónicos activos, normalizados a
     * minúsculas y sin duplicados.
     *
     * <p>
     * El resultado se guarda en la caché {@value #CACHE_ACTIVE_EMAILS}. La
     * opción {@code sync = true} garantiza que, si múltiples hilos solicitan
     * este dato cuando la caché está vacía, solo uno ejecutará la consulta a la
     * base de datos, bloqueando a los demás hasta que el resultado esté
     * disponible.</p>
     *
     * @return un {@link Set} inmutable de {@code String} con los correos
     * activos; si no hay datos, un conjunto vacío. Nunca es {@code null}.
     */
    @Cacheable(sync = true, cacheNames = CACHE_ACTIVE_EMAILS)
    public Set<String> getActiveEmails() {
        List<String> list = credentialRepository.findActiveEmailsInLowerCase();

        if (CollectionUtils.isEmpty(list)) {
            return Set.of();
        }

        // Filtrado defensivo: Set.copyOf lanza IllegalArgumentException si hay nulos.
        // Esto protege la aplicación ante posibles inconsistencias en los datos de la BD.
        return list.stream()
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Recupera una credencial utilizando su token, con soporte de caché.
     *
     * @param token El identificador único de tipo UUID.
     * @return Un {@link Optional} conteniendo la credencial si existe, o vacío
     * si no.
     */
    @Cacheable(cacheNames = CACHE_CREDENTIAL_BY_TOKEN, sync = true)
    public Optional<Credential> getCredentialByToken(final UUID token) {
        if (token == null) {
            return Optional.empty();
        }
        return credentialRepository.findByToken(token);
    }

    /**
     * Recupera una credencial utilizando su correo electrónico, con soporte de
     * caché.
     *
     * @param email La dirección de correo electrónico a buscar.
     * @return Un {@link Optional} conteniendo la credencial si existe, o vacío
     * si no.
     */
    @Cacheable(cacheNames = CACHE_CREDENTIAL_BY_EMAIL, sync = true)
    public Optional<Credential> getCredentialByEmail(final String email) {
        // Validación explícita y legible antes de invocar librerías externas o la BD
        if (email == null || email.isBlank() || !EmailValidator.getInstance().isValid(email)) {
            return Optional.empty();
        }
        return credentialRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Elimina todas las entradas de la caché de correos activos.
     *
     * <p>
     * La próxima llamada a {@link #getActiveEmails()} volverá a consultar la
     * base de datos. Debe invocarse después de modificar el estado de una
     * credencial (ej. activación, desactivación o eliminación de un
     * usuario).</p>
     */
    @CacheEvict(allEntries = true, cacheNames = CACHE_ACTIVE_EMAILS)
    public void clearActiveEmailsCache() {
        LOGGER.info("Invalidando caché de correos activos por cambio en el dominio");
    }
}
