package cl.utem.inf.cpyd.conf;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la caché en memoria de la aplicación, basada en Caffeine.
 *
 * <p>
 * La caché guarda el conjunto de códigos de curso válidos, que se consulta en
 * cada solicitud y cambia con muy poca frecuencia. Mantenerlo en memoria evita
 * una consulta a la base de datos por petición. Se usa Caffeine porque ofrece
 * una caché local, thread-safe y con expiración configurable, sin necesidad de
 * un servidor externo como Redis.</p>
 *
 * <p>
 * Los parámetros se leen de {@code application.properties} y se
 * restringen a valores mínimos, para que una mala configuración no deje la
 * caché sin efecto ni la haga expirar de inmediato:</p>
 * <ul>
 * <li>{@code api.cache.expires.minutes} (por defecto {@code 17}): minutos antes
 * de que una entrada expire desde su escritura. Mínimo efectivo:
 * {@value #MIN_EXPIRE_MINUTES}.</li>
 * <li>{@code api.cache.pool.size} (por defecto {@code 100}): máximo de entradas
 * almacenadas. Mínimo efectivo: {@value #MIN_POOL_SIZE}.</li>
 * </ul>
 *
 * <h2>Glosario de Términos</h2>
 * <dl>
 * <dt>Caché</dt>
 * <dd>Copia temporal de datos guardada en memoria para responder más rápido
 * sin volver a consultar la fuente original.</dd>
 * <dt>Expiración</dt>
 * <dd>Tiempo tras el cual un dato guardado en caché se descarta y debe volver a
 * obtenerse de la base de datos.</dd>
 * </dl>
 *
 * @author Sebastián Salazar Molina
 * @email ssalazar@utem.cl
 * @see <a href="https://orcid.org/0009-0006-1498-5658">ORCID: 0009-0006-1498-5658</a>
 * @institution Universidad Tecnológica Metropolitana (UTEM)
 * @version 0.0.1-SNAPSHOT
 * @since 0.0.1
 */
@Configuration
@EnableCaching
public class CacheConf {

    /**
     * Nombre lógico de la caché única de la aplicación. Es el valor que deben
     * usar las anotaciones {@code @Cacheable} y {@code @CacheEvict}.
     */
    public static final String CACHE_NAME = "excCache";

    /**
     * Tiempo mínimo de expiración, en minutos, aceptado aunque la propiedad
     * configurada sea menor.
     */
    private static final int MIN_EXPIRE_MINUTES = 17;

    /**
     * Número mínimo de entradas aceptado aunque la propiedad configurada sea
     * menor.
     */
    private static final long MIN_POOL_SIZE = 100;

    /**
     * Minutos de expiración efectivos, ya ajustados al mínimo.
     */
    private final int expireMinutes;

    /**
     * Número máximo efectivo de entradas, ya ajustado al mínimo.
     */
    private final long maxSize;

    /**
     * Construye la configuración de la caché y aplica los mínimos de seguridad.
     *
     * <p>
     * Los valores se ajustan aquí, en el constructor, y no en cada uso, para que
     * el resto de la aplicación siempre reciba valores ya válidos.</p>
     *
     * @param cacheExpireMinutes minutos de expiración configurados, de tipo
     * {@code int}; se toma el mayor entre este valor y {@value #MIN_EXPIRE_MINUTES}.
     * @param cacheMaxSize número máximo de entradas configurado, de tipo
     * {@code long}; se toma el mayor entre este valor y {@value #MIN_POOL_SIZE}.
     */
    public CacheConf(@Value("${api.cache.expires.minutes:17}") int cacheExpireMinutes,
            @Value("${api.cache.pool.size:100}") long cacheMaxSize) {
        this.expireMinutes = Math.max(cacheExpireMinutes, MIN_EXPIRE_MINUTES);
        this.maxSize = Math.max(cacheMaxSize, MIN_POOL_SIZE);
    }

    /**
     * Crea el administrador de caché que Spring utiliza para las anotaciones de
     * caché.
     *
     * <p>
     * Se declara una única caché con nombre {@link #CACHE_NAME}, con expiración
     * tras escritura, tamaño máximo y registro de estadísticas
     * ({@code recordStats}), que permite medir aciertos y fallos.</p>
     *
     * @return el {@link CacheManager} de tipo {@link CaffeineCacheManager}
     * configurado con la caché {@link #CACHE_NAME}; nunca {@code null}.
     */
    @Bean
    public CacheManager cacheManager() {
        final Caffeine<Object, Object> femsaCaffeineBuilder = Caffeine.newBuilder()
                .expireAfterWrite(this.expireMinutes, TimeUnit.MINUTES)
                .maximumSize(this.maxSize)
                .recordStats();

        final CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_NAME);
        cacheManager.setCaffeine(femsaCaffeineBuilder);

        return cacheManager;
    }
}
