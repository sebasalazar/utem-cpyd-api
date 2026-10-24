package cl.utem.inf.cpyd.api.v1;

import cl.utem.inf.cpyd.domain.data.ExchangeData;
import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.exceptions.NoDataException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import cl.utem.inf.cpyd.manager.ExchangeManager;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/exchanges")
public class ExchangeRest {

    private final ExchangeManager exchangeManager;

    public ExchangeRest(final ExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    private Country convert(final String countryStr) {
        if (StringUtils.isEmpty(countryStr)) {
            throw new ValidationException("El país es nulo");
        }

        final String value = StringUtils.upperCase(StringUtils.trimToEmpty(countryStr));
        switch (value) {
            case "CHILE" -> {
                return Country.CHILE;
            }
            case "COLOMBIA" -> {
                return Country.COLOMBIA;
            }
            case "ECUADOR" -> {
                return Country.ECUADOR;
            }
            default -> {
                final String logCountry = StringEscapeUtils.escapeJava(value);
                throw new ValidationException(String.format("El valor de país (%s) no está soportado", logCountry));
            }
        }
    }

    @GetMapping(value = {"/{country}/{exchange_date}"})
    public ResponseEntity<ExchangeData> exchangeByCountryAndDate(@PathVariable("country") String countryStr, @PathVariable("exchange_date") LocalDate date) {
        final Country country = convert(countryStr);
        Optional<Exchange> optional = exchangeManager.getExchange(country, date);
        if (optional.isEmpty()) {
            throw new NoDataException("No hay datos para las fecha y el país indicado");
        }

        return ResponseEntity.ok(new ExchangeData(optional.get()));
    }
}
