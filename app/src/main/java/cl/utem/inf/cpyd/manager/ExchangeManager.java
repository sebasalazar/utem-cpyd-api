package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.domain.repository.ExchangeRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ExchangeManager {

    private final ExchangeRepository exchangeRepository;

    public ExchangeManager(ExchangeRepository exchangeRepository) {
        this.exchangeRepository = exchangeRepository;
    }

    public Optional<Exchange> getExchange(final Country country, final LocalDate date) {
        if (country == null) {
            return Optional.empty();
        }

        if (date == null) {
            return Optional.empty();
        }

        return exchangeRepository.findByCountryAndDate(country, date);
    }
}
