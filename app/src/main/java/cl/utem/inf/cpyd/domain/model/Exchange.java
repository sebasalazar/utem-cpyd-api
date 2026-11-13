package cl.utem.inf.cpyd.domain.model;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.enums.CountryConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad JPA de una cotización: el tipo de cambio de un país para una fecha, mapeada
 * a la tabla {@code exchanges}.
 *
 * <p>La clave natural es la pareja (país, fecha). La base de datos garantiza con un
 * índice único que no existan dos cotizaciones del mismo país el mismo día. En Java,
 * la identidad también usa esa pareja y no el identificador técnico; ver
 * {@link #equals(Object)}.</p>
 */
@Entity
@Table(name = "exchanges")
@SuppressWarnings("java:S2160") // equals y hashCode se redefinen con la clave natural (país, fecha)
public class Exchange extends PkEntityBase {

    /** País de la cotización, persistido mediante {@link CountryConverter}. */
    @Convert(converter = CountryConverter.class)
    @Column(name = "ex_country", nullable = false)
    private Country country;

    /** Fecha de la cotización. */
    @Column(name = "ex_date", nullable = false)
    private LocalDate date;

    /** Unidades de moneda local por 1 USD, con dos decimales en la base de datos. */
    @Column(name = "ex_rate", nullable = false)
    private BigDecimal rate;

    /**
     * Constructor requerido por JPA. Deja valores por defecto (país nulo, fecha actual y
     * tasa cero) que Hibernate sobrescribe al cargar la fila. No debe usarse en código de
     * negocio.
     */
    public Exchange() {
        this.country = null;
        this.date = LocalDate.now();
        this.rate = BigDecimal.ZERO;
    }

    /**
     * Crea una cotización completa.
     *
     * @param country país.
     * @param date fecha de la cotización.
     * @param rate unidades de moneda local por 1 USD.
     */
    public Exchange(Country country, LocalDate date, BigDecimal rate) {
        this.country = country;
        this.date = date;
        this.rate = rate;
    }

    /**
     * Devuelve el país.
     *
     * @return país.
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Asigna el país.
     *
     * @param country país.
     */
    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * Devuelve la fecha de la cotización.
     *
     * @return fecha.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Asigna la fecha de la cotización.
     *
     * @param date fecha.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Devuelve la tasa.
     *
     * @return unidades de moneda local por 1 USD.
     */
    public BigDecimal getRate() {
        return rate;
    }

    /**
     * Asigna la tasa.
     *
     * @param rate unidades de moneda local por 1 USD.
     */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    /**
     * Código hash calculado a partir de país y fecha, coherente con {@link #equals(Object)}.
     *
     * @return código hash.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.country);
        hash = 29 * hash + Objects.hashCode(this.date);
        return hash;
    }

    /**
     * Dos cotizaciones son iguales si son de la misma clase, con el mismo país y la misma
     * fecha. La tasa no interviene: así se modela la clave natural.
     *
     * @param obj objeto a comparar.
     * @return {@code true} si representan el mismo país y día.
     */
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
