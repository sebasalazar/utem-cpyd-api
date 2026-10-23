package cl.utem.inf.cpyd.task.helper;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.domain.repository.ExchangeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangeHelper {

    private final ExchangeRepository exchangeRepository;

    public record CsvRow(Country country, LocalDate date, BigDecimal rate) {}

    public ExchangeHelper(ExchangeRepository exchangeRepository) {
        this.exchangeRepository = exchangeRepository;
    }

    public Optional<Exchange> getExchange(Country country, LocalDate date) {
        if (country == null) {
            return Optional.empty();
        }

        if (date == null) {
            return Optional.empty();
        }

        return exchangeRepository.findByCountryAndDate(country, date);
    }

    @Transactional
    public void saveAll(final List<CsvRow> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<Exchange> exchanges = new ArrayList<>();
        for (CsvRow row : list) {
            Optional<Exchange> opt = exchangeRepository.findByCountryAndDate(row.country(), row.date());
            if (opt.isPresent()) {
                Exchange exchange = opt.get();
                exchange.setRate(row.rate());
                exchanges.add(exchange);
            } else {
                exchanges.add(new Exchange(row.country(), row.date(), row.rate()));
            }
        }

        exchangeRepository.saveAll(exchanges);
    }
}
