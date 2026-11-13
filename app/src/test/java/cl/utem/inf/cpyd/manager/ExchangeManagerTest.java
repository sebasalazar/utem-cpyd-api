package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.domain.repository.ExchangeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExchangeManagerTest {

    private final ExchangeRepository repo = mock(ExchangeRepository.class);
    private final ExchangeManager manager = new ExchangeManager(repo);
    private final LocalDate d = LocalDate.of(2025, 1, 2);

    @Test
    void nullArgumentsShortCircuit() {
        assertTrue(manager.getExchange(null, d).isEmpty());
        assertTrue(manager.getExchange(Country.CHILE, null).isEmpty());
        assertTrue(manager.getExchange(null, d, d).isEmpty());
        verifyNoInteractions(repo);
    }

    @Test
    void delegatesToRepository() {
        final Exchange e = new Exchange(Country.CHILE, d, new BigDecimal("900.10"));
        when(repo.findByCountryAndDate(Country.CHILE, d)).thenReturn(Optional.of(e));
        when(repo.findByCountryAndOptionalDateRange(Country.CHILE, d, d)).thenReturn(List.of(e));

        assertEquals(e, manager.getExchange(Country.CHILE, d).orElseThrow());
        assertEquals(1, manager.getExchange(Country.CHILE, d, d).size());
    }
}
