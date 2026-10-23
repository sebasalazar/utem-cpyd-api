package cl.utem.inf.cpyd.domain.model;

import cl.utem.inf.cpyd.domain.enums.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "exchanges")
@SuppressWarnings("java:S2160") // La clase base IdEntityBase implementa un contrato de equals/hashCode
public class Exchange extends PkEntityBase {

    @Column(name = "ex_country", nullable = false)
    private Country country;

    @Column(name = "ex_date", nullable = false)
    private LocalDate date;

    @Column(name = "ex_rate", nullable = false)
    private BigDecimal rate;

    public Exchange() {
        this.country = null;
        this.date = LocalDate.now();
        this.rate = BigDecimal.ZERO;
    }

    public Exchange(Country country, LocalDate date, BigDecimal rate) {
        this.country = country;
        this.date = date;
        this.rate = rate;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.country);
        hash = 29 * hash + Objects.hashCode(this.date);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Exchange other = (Exchange) obj;
        if (this.country != other.country) {
            return false;
        }
        return Objects.equals(this.date, other.date);
    }
}
