package cl.utem.inf.cpyd.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TextAndUuidUtilsTest {

    @Test
    void textNormalization() {
        assertEquals("a b", TextUtils.normalize("  a    b  "));
        assertEquals("", TextUtils.normalize(null));
        assertEquals("", TextUtils.normalize("   "));
        assertEquals("USER@UTEM.CL", TextUtils.upper(" user@utem.cl "));
        assertEquals("user@utem.cl", TextUtils.lower(" User@UTEM.cl "));
        assertEquals("", TextUtils.lower(null));
    }

    @Test
    void sanitizeEscapesAndTruncates() {
        assertEquals("", TextUtils.sanitize(null));
        assertEquals("", TextUtils.sanitize(" "));
        assertFalse(TextUtils.sanitize("a\nb\"c").contains("\n"));
        assertTrue(TextUtils.sanitize("x".repeat(10_000)).length() < 10_000);
    }

    @Test
    void uuidV7() {
        final UUID u = UuidUtils.generateV7();
        assertTrue(UuidUtils.isV7(u));
        assertTrue(UuidUtils.isV7(UUID.fromString(UuidUtils.generateV7AsString())));
        assertFalse(UuidUtils.isV7(UUID.randomUUID()));
        assertFalse(UuidUtils.isV7(null));

        final Duration drift = Duration.between(UuidUtils.extractTimestamp(u), Instant.now()).abs();
        assertTrue(drift.getSeconds() < 5);
        assertThrows(IllegalArgumentException.class, () -> UuidUtils.extractTimestamp(UUID.randomUUID()));
        assertThrows(NullPointerException.class, () -> UuidUtils.extractTimestamp(null));
    }
}
