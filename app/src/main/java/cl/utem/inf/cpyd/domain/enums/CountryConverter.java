package cl.utem.inf.cpyd.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persiste {@link Country} usando su id estable en vez del ordinal del enum.
 */
@Converter
public class CountryConverter implements AttributeConverter<Country, Short> {

    /**
     * Convierte el país a su id para la columna {@code smallint}.
     *
     * @param country país, o {@code null}.
     * @return el id (0 a 2), o {@code null} si el país es nulo.
     */
    @Override
    public Short convertToDatabaseColumn(final Country country) {
        return country == null ? null : (short) country.getId();
    }

    /**
     * Convierte el valor de la columna en el país correspondiente.
     *
     * @param id valor leído de la base de datos, o {@code null}.
     * @return el país, o {@code null} si el valor es nulo.
     * @throws IllegalArgumentException si el id no corresponde a ningún país.
     */
    @Override
    public Country convertToEntityAttribute(final Short id) {
        return id == null ? null : Country.fromId(id);
    }
}
