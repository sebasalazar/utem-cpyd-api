package cl.utem.inf.cpyd.domain.data;

import cl.utem.inf.cpyd.domain.model.Exchange;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExchangeData {

    private String country;
    private LocalDate date;
    private BigDecimal rate;

    public ExchangeData() {
    }

    public ExchangeData(Exchange ex) {
        this.country = ex.getCountry().getName();
        this.date = ex.getDate();
        this.rate = ex.getRate();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
