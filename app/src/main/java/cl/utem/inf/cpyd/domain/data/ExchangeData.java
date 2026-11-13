package cl.utem.inf.cpyd.domain.data;

import cl.utem.inf.cpyd.domain.model.Exchange;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida en JSON para una cotización.
 *
 * <p>Separa la representación pública de la entidad JPA: la API no expone el
 * identificador interno ni los campos de auditoría, y el país aparece con su nombre
 * (por ejemplo, {@code Chile}) en lugar de su código numérico.</p>
 */
@Schema(description = "Cotización del dólar en un país para una fecha")
public class ExchangeData {

    @Schema(description = "Nombre del país", example = "Chile")
    private String country;

    @Schema(description = "Fecha de la cotización (yyyy-MM-dd)", example = "2025-01-02")
    private LocalDate date;

    @Schema(description = "Unidades de moneda local por 1 USD (CLP, COP). En Ecuador vale siempre 1.00",
            example = "995.30")
    private BigDecimal rate;

    /**
     * Constructor vacío, necesario para la deserialización JSON.
     */
    public ExchangeData() {
    }

    /**
     * Construye el DTO a partir de la entidad.
     *
     * @param ex entidad de cotización. No debe ser {@code null}, y su país tampoco.
     */
    public ExchangeData(Exchange ex) {
        this.country = ex.getCountry().getName();
        this.date = ex.getDate();
        this.rate = ex.getRate();
    }

    /**
     * Devuelve el nombre del país.
     *
     * @return nombre del país.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Asigna el nombre del país.
     *
     * @param country nombre del país.
     */
    public void setCountry(String country) {
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
     * Devuelve la tasa: unidades de moneda local por 1 USD.
     *
     * @return tasa.
     */
    public BigDecimal getRate() {
        return rate;
    }

    /**
     * Asigna la tasa de cambio.
     *
     * @param rate tasa.
     */
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
