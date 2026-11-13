package cl.utem.inf.cpyd.domain.repository;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para la gestión de entidades Exchange (Tipos de Cambio). Extiende
 * JpaRepository para operaciones CRUD estándar y define consultas específicas
 * para la búsqueda por país y rangos de fechas.
 *
 * @author Sebastián Salazar Molina
 * @email ssalazar@utem.cl
 * @see <a href="https://orcid.org/0009-0006-1498-5658">ORCID:
 * 0009-0006-1498-5658</a>
 * @institution Universidad Tecnológica Metropolitana (UTEM)
 * @version 0.0.2-SNAPSHOT
 * @since 0.0.1
 */
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    /**
     * Busca un registro de tipo de cambio específico para un país y una fecha
     * exacta.
     *
     * @param country El país para el cual se busca el tipo de cambio.
     * @param date La fecha exacta del tipo de cambio.
     * @return Un {@link Optional} conteniendo el registro si existe, o vacío si
     * no.
     */
    Optional<Exchange> findByCountryAndDate(Country country, LocalDate date);

    /**
     * Busca registros de tipo de cambio para un país dentro de un rango de
     * fechas opcional.
     *
     * <p>
     * Si {@code start} es {@code null}, no se aplica límite inferior de fecha.
     * Si {@code end} es {@code null}, no se aplica límite superior de fecha. Si
     * ambos son {@code null}, devuelve todos los registros para el país
     * especificado.</p>
     *
     * @param country El país para el cual se busca el tipo de cambio
     * (obligatorio).
     * @param start La fecha de inicio del rango (inclusive). Puede ser
     * {@code null}.
     * @param end La fecha de fin del rango (inclusive). Puede ser {@code null}.
     * @return Una {@link List} de registros que coinciden con los criterios.
     * Nunca es {@code null}; puede estar vacía si no hay coincidencias.
     */
    @Query("SELECT e FROM Exchange e WHERE e.country = :country "
            + "AND (:start IS NULL OR e.date >= :start) "
            + "AND (:end IS NULL OR e.date <= :end)")
    List<Exchange> findByCountryAndOptionalDateRange(
            @Param("country") Country country,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
