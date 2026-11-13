package cl.utem.inf.cpyd.domain;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.enums.CountryConverter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CountryTest {

    @Test
    void persistedIdsAreStable() {
        // Estos valores ya existen en la base de datos: no deben cambiar.
        assertEquals(0, Country.CHILE.getId());
        assertEquals(1, Country.COLOMBIA.getId());
        assertEquals(2, Country.ECUADOR.getId());
    }

    @Test
    void fromIdRoundTrip() {
        for (Country c : Country.values()) {
            assertEquals(c, Country.fromId(c.getId()));
        }
        assertThrows(IllegalArgumentException.class, () -> Country.fromId(99));
    }

    @Test
    void converterHandlesNullsAndValues() {
        final CountryConverter conv = new CountryConverter();
        assertNull(conv.convertToDatabaseColumn(null));
        assertNull(conv.convertToEntityAttribute(null));
        assertEquals((short) 1, conv.convertToDatabaseColumn(Country.COLOMBIA));
        assertEquals(Country.ECUADOR, conv.convertToEntityAttribute((short) 2));
    }
}
