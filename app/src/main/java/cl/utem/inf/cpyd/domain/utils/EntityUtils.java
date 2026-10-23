package cl.utem.inf.cpyd.domain.utils;

import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;

/**
 * Utilidades transversales para el manejo de entidades JPA.
 *
 * <p>
 * Esta clase centraliza la resolución de la clase persistente de una entidad,
 * operación necesaria en toda implementación correcta de
 * {@link Object#equals(Object)} sobre entidades gestionadas por Hibernate. Al
 * estar aquí, tanto las entidades con clave generada ({@code IdEntityBase})
 * como las entidades con clave asignada ({@code Periodo}) comparten exactamente
 * el mismo criterio, sin duplicación.</p>
 *
 * <p>
 * <strong>Glosario:</strong></p>
 * <ul>
 * <li><em>Proxy de Hibernate:</em> objeto sustituto que Hibernate crea para
 * representar una entidad que aún no ha sido cargada desde la base de datos. Su
 * clase Java real no es la de la entidad, sino una subclase generada en tiempo
 * de ejecución con un nombre del tipo
 * {@code Periodo$HibernateProxy$aB3xK}.</li>
 * <li><em>Clase persistente:</em> la clase de entidad que el proxy representa,
 * es decir, la que interesa para comparaciones de igualdad.</li>
 * <li><em>Inicializar un proxy:</em> obligar a Hibernate a ejecutar la consulta
 * que carga los datos reales. Es una operación con costo, y falla con
 * {@code LazyInitializationException} si la sesión ya se cerró.</li>
 * </ul>
 *
 * @author Equipo de desarrollo de ExperTI
 * @since 1.1.0
 * @version 1.0.0
 */
public final class EntityUtils {

    

    /**
     * Constructor privado: esta clase no está diseñada para ser instanciada.
     *
     * @throws AssertionError siempre que se intente instanciar, incluso por
     * reflexión.
     */
    private EntityUtils() {
        throw new AssertionError("EntityUtils no es instanciable");
    }

    /**
     * Resuelve la clase persistente de una entidad, desenvolviendo proxies de
     * Hibernate sin forzar su inicialización.
     *
     * <p>
     * A diferencia de {@code org.hibernate.Hibernate#getClass(Object)}, que
     * invoca {@code getImplementation()} y por tanto materializa el proxy, este
     * método consulta únicamente los metadatos del inicializador perezoso. Como
     * consecuencia, comparar contra una referencia obtenida mediante
     * {@code EntityManager#getReference} no dispara consultas adicionales ni
     * lanza {@code LazyInitializationException} fuera de sesión.</p>
     *
     * <p>
     * <strong>Limitación conocida:</strong> {@code getPersistentClass()}
     * devuelve el tipo declarado de la asociación. En jerarquías de herencia de
     * entidades ({@code SINGLE_TABLE} o {@code JOINED}), un proxy declarado
     * como el tipo base devolverá el tipo base y no la subclase concreta. Si el
     * modelo de dominio incorpora herencia de entidades, este método debe
     * revisarse.</p>
     *
     * @param entity la entidad o proxy a inspeccionar
     * @return la clase persistente asociada a la entidad, nunca {@code null}
     * @throws NullPointerException si {@code entity} es {@code null}
     */
    public static Class<?> persistentClass(final Object entity) {
        Objects.requireNonNull(entity, "entity no puede ser null");

        if (entity instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }

    /**
     * Indica si dos objetos corresponden a la misma clase persistente.
     *
     * <p>
     * Método de conveniencia para implementaciones de
     * {@link Object#equals(Object)}. Devuelve {@code false} si cualquiera de
     * los dos argumentos es {@code null}, evitando que el llamador deba
     * encadenar verificaciones previas.</p>
     *
     * @param first primer objeto a comparar; puede ser {@code null}
     * @param second segundo objeto a comparar; puede ser {@code null}
     * @return {@code true} si ambos son no nulos y comparten clase persistente
     */
    public static boolean samePersistentClass(final Object first, final Object second) {
        if (first == null || second == null) {
            return false;
        }
        return persistentClass(first) == persistentClass(second);
    }
}
