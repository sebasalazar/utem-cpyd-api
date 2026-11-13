package cl.utem.inf.cpyd.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Utilitario para generar y analizar UUID versión 7 (RFC 9562).
 * <p>
 * Un UUID v7 incluye el timestamp de creación en sus primeros 48 bits, lo que
 * los hace ordenables cronológicamente y eficientes como clave primaria en
 * índices B-tree (PostgreSQL, Oracle). Adicionalmente, al ser ordenables por
 * tiempo de generación, reducen la fragmentación de páginas y mejoran el
 * rendimiento de escritura en comparación con UUID v4.
 * </p>
 * <p>
 * Esta clase expone operaciones para:
 * </p>
 * <ul>
 * <li>Generar UUID v7 como {@link UUID} o como {@link String}.</li>
 * <li>Verificar si un {@link UUID} dado es de versión 7.</li>
 * <li>Extraer el {@link Instant} de creación embebido en un UUID v7.</li>
 * </ul>
 * <p>
 * <b>Thread-safety:</b> el generador subyacente
 * ({@link TimeBasedEpochGenerator}) es seguro para uso concurrente y garantiza
 * orden monotónico incluso dentro del mismo milisegundo en una misma JVM.
 * </p>
 * <p>
 * <b>Nota:</b> esta clase es utilitaria y no debe instanciarse.
 * </p>
 *
 * @author ExperTI
 * @since 2.1.26257
 * @see UUID
 * @see Instant
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9562">RFC 9562</a>
 */
public final class UuidUtils {

    /**
     * Número de versión de UUID soportado por este utilitario (v7).
     */
    private static final int UUID_VERSION_7 = 7;

    /**
     * Desplazamiento de bits necesario para extraer el timestamp de los 48 bits
     * más significativos de un UUID v7.
     * <p>
     * Los primeros 48 bits de los {@code mostSignificantBits} contienen el
     * timestamp en milisegundos; para aislarlo se desplaza a la derecha
     * {@code 16} bits (los restantes 64 - 48 = 16 bits corresponden a la
     * versión y a los bits de variante/aleatorios).
     * </p>
     */
    private static final int TIMESTAMP_SHIFT_BITS = 16;

    /**
     * Instancia única y thread-safe. Garantiza orden monotónico dentro del
     * mismo milisegundo en esta JVM.
     */
    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    /**
     * Constructor privado para prevenir la instanciación de esta clase
     * utilitaria.
     *
     * @throws AssertionError siempre, si se invoca mediante reflexión
     */
    private UuidUtils() {
        throw new AssertionError("Clase utilitaria, no debe instanciarse");
    }

    /**
     * Genera un nuevo UUID versión 7.
     * <p>
     * El UUID resultante incorpora el timestamp actual (en milisegundos) en sus
     * primeros 48 bits, por lo que es ordenable cronológicamente y apropiado
     * como clave primaria en bases de datos con índices B-tree.
     * </p>
     *
     * @return un nuevo {@link UUID} versión 7; nunca {@code null}
     */
    public static UUID generateV7() {
        return GENERATOR.generate();
    }

    /**
     * Genera un nuevo UUID versión 7 y lo devuelve en su representación textual
     * estándar.
     * <p>
     * El formato devuelto corresponde a la representación canónica de
     * {@link UUID#toString()}: {@code xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx} (36
     * caracteres, en minúsculas y con guiones).
     * </p>
     *
     * @return representación textual del UUID v7 generado; nunca {@code null}
     */
    public static String generateV7AsString() {
        UUID uuid = generateV7();
        return uuid.toString();
    }

    /**
     * Verifica si un UUID es de versión 7.
     *
     * @param uuid UUID a evaluar; puede ser {@code null}
     * @return {@code true} si {@code uuid} no es {@code null} y su versión es
     * {@value #UUID_VERSION_7}; {@code false} en caso contrario
     */
    public static boolean isV7(UUID uuid) {
        return (uuid != null && uuid.version() == UUID_VERSION_7);
    }

    /**
     * Extrae el instante de creación embebido en un UUID v7.
     * <p>
     * El timestamp se obtiene tomando los 48 bits más significativos del UUID
     * (desplazando {@code mostSignificantBits} a la derecha
     * {@value #TIMESTAMP_SHIFT_BITS} posiciones) y convirtiéndolos a
     * milisegundos desde la época Unix.
     * </p>
     *
     * @param uuid UUID v7 del que se desea extraer el timestamp; no puede ser
     * {@code null}
     * @return el {@link Instant} correspondiente al momento de creación del
     * UUID
     * @throws NullPointerException si {@code uuid} es {@code null}
     * @throws IllegalArgumentException si {@code uuid} no es de versión 7
     */
    public static Instant extractTimestamp(UUID uuid) {
        Objects.requireNonNull(uuid, "El UUID no puede ser nulo");
        if (!isV7(uuid)) {
            throw new IllegalArgumentException("El UUID no es versión 7: " + uuid);
        }
        long timestampMillis = uuid.getMostSignificantBits() >>> TIMESTAMP_SHIFT_BITS;
        return Instant.ofEpochMilli(timestampMillis);
    }
}
