package cl.utem.inf.cpyd.domain.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Fuente única de tiempo en UTC para las columnas de auditoría del modelo de
 * dominio.
 *
 * <p>
 * Esta clase expone un punto único para obtener el instante actual usado en
 * campos como {@code created} y {@code updated}. El reloj por defecto está
 * fijado en UTC de forma deliberada: ver {@link #DEFAULT_CLOCK} para el detalle
 * de esa decisión.
 * </p>
 *
 * <p>
 * <strong>Por qué no usar la zona horaria del proceso:</strong> la aplicación
 * define su zona horaria vía la variable de entorno {@code TZ} (p. ej.
 * {@code America/Santiago}), y esa configuración es correcta para la capa de
 * presentación —formateo de fechas para personas, cálculo de "día calendario"
 * local— pero no para marcas de auditoría. Las fechas del cambio de hora en
 * Chile están fijadas por decreto del Ejecutivo, sujeto a modificación: el
 * decreto vigente ya ha sido reemplazado más de una vez en años recientes. Un
 * instante calculado con {@code ZoneId.systemDefault()} puede además caer en un
 * rango ambiguo o inexistente durante la noche de la transición, cuando el
 * reloj local salta o repite una hora. Un instante en UTC es estable frente a
 * ambos problemas: no depende de en qué mitad del año, ni en qué nodo, se
 * ejecutó el {@code INSERT}. La conversión a hora local, cuando se necesita
 * mostrarla a un usuario, debe hacerse en la capa de presentación con
 * {@code ZoneId.systemDefault()} a partir del instante ya persistido.
 * </p>
 *
 * <p>
 * <strong>Glosario:</strong></p>
 * <ul>
 * <li><em>UTC:</em> tiempo universal coordinado. En esta clase se usa como
 * referencia temporal con offset cero.</li>
 * <li><em>Clock:</em> abstracción de Java para obtener el tiempo actual.
 * Permite reemplazar el reloj real por uno fijo en pruebas.</li>
 * <li><em>OffsetDateTime:</em> fecha-hora con offset respecto a UTC. En esta
 * utilidad el offset devuelto es siempre {@code Z}.</li>
 * </ul>
 *
 * <p>
 * Esta clase es thread-safe: no mantiene estado mutable y el reloj por defecto
 * es inmutable.
 * </p>
 *
 * @author ExperTI
 * @since 2.1.0.26223
 */
public final class AuditClock {

    /**
     * Reloj por defecto usado por {@link #now()}. Fijado en UTC de forma
     * deliberada: no depende de la zona horaria del sistema operativo, de la
     * variable de entorno {@code TZ}, ni del nodo que ejecute la operación.
     */
    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

    /**
     * Constructor privado: esta clase no está diseñada para ser instanciada.
     *
     * @throws AssertionError siempre que se intente instanciar, incluso por
     * reflexión.
     */
    private AuditClock() {
        throw new AssertionError("AuditClock no es instanciable");
    }

    /**
     * Devuelve el instante actual en UTC, según el reloj por defecto de la
     * aplicación.
     *
     * <p>
     * Todas las marcas de auditoría del modelo deben obtenerse por esta vía y
     * no llamando directamente a {@code OffsetDateTime.now()}, para que la
     * política temporal quede en un solo punto.</p>
     *
     * @return el instante actual con offset {@code Z}
     */
    public static OffsetDateTime now() {
        return now(DEFAULT_CLOCK);
    }

    /**
     * Devuelve el instante actual en UTC usando el reloj indicado.
     *
     * <p>
     * Pensado para pruebas: un test puede pasar un
     * {@code Clock.fixed(instante, ZoneOffset.UTC)} y verificar con un assert
     * exacto el valor devuelto. El resultado siempre tendrá offset {@code Z},
     * incluso si el reloj recibido tiene una zona horaria distinta.
     * </p>
     *
     * @param clock reloj a utilizar; no puede ser {@code null}
     * @return el instante actual con offset {@code Z}
     * @throws NullPointerException si {@code clock} es {@code null}
     */
    public static OffsetDateTime now(final Clock clock) {
        Objects.requireNonNull(clock, "clock no puede ser null");
        return Instant.now(clock).atOffset(ZoneOffset.UTC);
    }
}
