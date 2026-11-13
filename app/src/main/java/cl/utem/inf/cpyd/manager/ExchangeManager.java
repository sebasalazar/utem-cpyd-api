package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.domain.repository.ExchangeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Servicio de consulta de tipos de cambio.
 *
 * <p>Es una capa delgada: valida argumentos nulos y delega en
 * {@link ExchangeRepository}. Separar esta capa del controlador permite probar la
 * regla de negocio con objetos simulados.</p>
 */
@Service
public class ExchangeManager {

    private final ExchangeRepository exchangeRepository;

    /**
     * Crea el servicio con su repositorio.
     *
     * @param exchangeRepository repositorio JPA de tipos de cambio.
     */
    public ExchangeManager(ExchangeRepository exchangeRepository) {
        this.exchangeRepository = exchangeRepository;
    }

    /**
     * Busca el tipo de cambio de un país en una fecha exacta.
     *
     * @param country país; si es {@code null}, devuelve vacío sin consultar la base de datos.
     * @param date fecha; si es {@code null}, devuelve vacío.
     * @return un {@link java.util.Optional} con el registro, o vacío si no existe.
     */
    public Optional<Exchange> getExchange(final Country country, final LocalDate date) {
        if (country == null) {
            return Optional.empty();
        }

        if (date == null) {
            return Optional.empty();
        }

        return exchangeRepository.findByCountryAndDate(country, date);
    }

    /**
     * Busca los tipos de cambio de un país dentro de un rango de fechas.
     *
     * @param country país; si es {@code null}, devuelve una lista vacía.
     * @param startDate fecha inicial, inclusive; {@code null} significa sin límite inferior.
     * @param endDate fecha final, inclusive; {@code null} significa sin límite superior.
     * @return lista de registros que cumplen el criterio. Nunca es {@code null}.
     */
    public List<Exchange> getExchange(final Country country, final LocalDate startDate, final LocalDate endDate) {
        if (country == null) {
            return List.of();
        }

        return exchangeRepository.findByCountryAndOptionalDateRange(country, startDate, endDate);
    }
}
