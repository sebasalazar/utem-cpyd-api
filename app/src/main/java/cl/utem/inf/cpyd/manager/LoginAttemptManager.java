package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.exceptions.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Limita los intentos fallidos de login por clave (correo en minúsculas).
 * Los contadores viven en memoria y expiran tras la ventana configurada.
 */
@Service
public class LoginAttemptManager {

    private final int maxAttempts;
    private final Cache<String, AtomicInteger> failures;

    /**
     * Configura el límite de intentos fallidos.
     *
     * <p>Los valores menores que 1 se ajustan a 1, de modo que una mala configuración
     * no deje el control desactivado.</p>
     *
     * @param maxAttempts fallos permitidos antes de bloquear la clave.
     * @param windowMinutes duración del conteo en minutos, medida desde el primer fallo.
     * Al expirar, el contador se reinicia.
     */
    public LoginAttemptManager(@Value("${cpyd.login.max-attempts:5}") int maxAttempts,
            @Value("${cpyd.login.window.minutes:15}") long windowMinutes) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.failures = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(Math.max(1, windowMinutes)))
                .maximumSize(10_000)
                .build();
    }

    /**
     * Verifica que la clave no haya superado el máximo de fallos en la ventana vigente.
     *
     * @param key clave de conteo (correo en minúsculas).
     * @throws TooManyRequestsException si la clave ya alcanzó el máximo de fallos.
     */
    public void checkAllowed(final String key) {
        final AtomicInteger count = failures.getIfPresent(key);
        if (count != null && count.get() >= maxAttempts) {
            throw new TooManyRequestsException();
        }
    }

    /**
     * Registra un intento fallido para la clave indicada. Crea el contador si no existe.
     *
     * @param key clave de conteo; en esta aplicación, el correo en minúsculas.
     */
    public void recordFailure(final String key) {
        failures.get(key, k -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Elimina el contador de una clave, por ejemplo tras un inicio de sesión exitoso.
     *
     * @param key clave cuyo contador se reinicia.
     */
    public void reset(final String key) {
        failures.invalidate(key);
    }
}
