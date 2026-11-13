package cl.utem.inf.cpyd.utils;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Utilidades para la manipulación y normalización de cadenas de texto.
 *
 * <p>
 * Centraliza la limpieza de la entrada del usuario (códigos de curso, RUT) para
 * que todas las comparaciones se hagan sobre una forma única. Sin esta
 * normalización, {@code " inf101 "} e {@code "INF101"} se tratarían como cursos
 * distintos. Todos los métodos son estáticos y no mantienen estado.</p>
 *
 * <p>
 * Los métodos de conversión de caso usan {@link Locale#ROOT} para que el
 * resultado no dependa de la configuración regional del servidor. Por ejemplo,
 * en una configuración turca la letra "i" no se convierte en "İ".</p>
 *
 * <h2>Glosario de Términos</h2>
 * <dl>
 * <dt>Normalizar</dt>
 * <dd>Llevar un texto a una forma estándar: sin espacios sobrantes y con un
 * formato uniforme, para poder compararlo de manera confiable.</dd>
 * <dt>Sanitizar</dt>
 * <dd>Preparar un texto para registrarlo en el log: se recorta a un largo máximo
 * y se escapan sus caracteres de control, de modo que no altere la salida.</dd>
 * </dl>
 *
 * @author Sebastián Salazar Molina
 * @email ssalazar@utem.cl
 * @see <a href="https://orcid.org/0009-0006-1498-5658">ORCID: 0009-0006-1498-5658</a>
 * @institution Universidad Tecnológica Metropolitana (UTEM)
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1
 */
public final class TextUtils {

    /**
     * Largo máximo, en caracteres, de un texto que se registra en el log.
     */
    private static final int MAX_LOG = 1024;

    /**
     * Constructor privado: esta clase solo contiene métodos estáticos.
     *
     * @throws UnsupportedOperationException siempre que se intente instanciar.
     */
    private TextUtils() {
        throw new UnsupportedOperationException("Clase utilitaria no instanciable");
    }

    /**
     * Normaliza el texto de entrada eliminando espacios en blanco al inicio y
     * al final, y colapsando múltiples espacios internos en un solo espacio.
     *
     * @param text Texto de entrada, de tipo {@code String} (puede ser nulo).
     * @return Una cadena no nula con los espacios normalizados. Retorna cadena
     * vacía si la entrada es nula o solo contiene espacios.
     */
    public static String normalize(final String text) {
        String normal = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(text)) {
            normal = StringUtils.normalizeSpace(StringUtils.trimToEmpty(text));
        }

        return normal;
    }

    /**
     * Convierte el texto a mayúsculas después de normalizarlo.
     *
     * @param text Texto de entrada, de tipo {@code String}.
     * @return Texto en mayúsculas y normalizado, de tipo {@code String}. Cadena
     * vacía si la entrada es nula.
     */
    public static String upper(final String text) {
        return StringUtils.upperCase(normalize(text), Locale.ROOT);
    }

    /**
     * Convierte el texto a minúsculas después de normalizarlo.
     *
     * @param text Texto de entrada, de tipo {@code String}.
     * @return Texto en minúsculas y normalizado, de tipo {@code String}. Cadena
     * vacía si la entrada es nula.
     */
    public static String lower(final String text) {
        return StringUtils.lowerCase(normalize(text), Locale.ROOT);
    }

    /**
     * Prepara un texto para registrarlo en el log de forma segura.
     *
     * <p>
     * Primero trunca el texto a {@value #MAX_LOG} caracteres, para que una
     * entrada muy larga no sature el log, y luego escapa las secuencias de
     * control (comillas, saltos de línea, tabulaciones) para que un usuario no
     * pueda falsificar líneas del registro. El truncamiento va antes del escape,
     * así el límite se aplica al texto original.</p>
     *
     * @param text Texto a sanitizar, de tipo {@code String}; puede ser nulo o
     * estar en blanco.
     * @return Texto truncado y escapado, de tipo {@code String}. Cadena vacía si
     * la entrada es nula o está en blanco.
     */
    public static String sanitize(final String text) {
        if (org.apache.commons.lang3.StringUtils.isBlank(text)) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }

        final String str = org.apache.commons.lang3.StringUtils.abbreviate(text, MAX_LOG);
        return StringEscapeUtils.escapeJava(str);
    }
}
