package cl.utem.inf.cpyd.domain.model;

import cl.utem.inf.cpyd.domain.utils.AuditClock;
import cl.utem.inf.cpyd.domain.utils.EntityUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Clase base abstracta para entidades JPA que utilizan una clave primaria
 * numérica autoincremental (generada por la base de datos).
 *
 * <p>
 * Esta clase proporciona un identificador {@code id} de tipo {@link Long} con
 * las anotaciones JPA estándar, y define un contrato de igualdad y código hash
 * basado en el identificador (para {@code equals}) y en la clase persistente
 * (para {@code hashCode}). Para entidades transientes (con {@code id == null}),
 * el método {@link #equals(Object)} devuelve {@code false} incluso al
 * compararse con otra instancia transiente (salvo que sea la misma referencia),
 * lo que previene errores comunes al usar colecciones antes de la
 * persistencia.</p>
 *
 * <p>
 * <strong>Contrato de igualdad:</strong></p>
 * <ul>
 * <li>Dos instancias se consideran iguales si y solo si su clase persistente es
 * la misma (no solo asignable) y tienen el mismo identificador no nulo.</li>
 * <li>Si alguna de las dos tiene {@code id == null}, el resultado es siempre
 * {@code false} (excepto si es la misma referencia, que se verifica
 * primero).</li>
 * <li>El método {@code hashCode} devuelve el hash de la clase persistente,
 * valor que <em>no depende del identificador</em> y por tanto permanece
 * constante durante el ciclo de vida de la entidad.</li>
 * </ul>
 *
 * <p>
 * <strong>Las subclases no deben sobrescribir {@code equals} ni
 * {@code hashCode}.</strong> Un override que use un atributo de negocio
 * mientras la entidad es transiente y el identificador una vez persistida
 * produce dos defectos: un hash mutable (la entidad se pierde dentro de un
 * {@code HashSet} tras el {@code persist}) y una igualdad asimétrica
 * ({@code a.equals(b) != b.equals(a)} cuando solo una de las dos tiene
 * identificador). Si una entidad necesita igualdad por clave natural, debe
 * implementarla de forma incondicional y no heredar de esta clase.</p>
 *
 * <p>
 * <strong>Por qué el hash no usa el identificador:</strong></p>
 * <p>
 * Si {@code hashCode} dependiera del {@code id}, su valor cambiaría en el
 * momento en que la entidad pasa de transiente a persistida. Una entidad
 * agregada a un {@code HashSet} antes del {@code persist} quedaría indexada en
 * un bucket calculado con el hash antiguo, y las llamadas posteriores a
 * {@code contains} o {@code remove} buscarían en un bucket distinto: la entidad
 * quedaría inaccesible dentro de la propia colección que la contiene.</p>
 * <p>
 * Devolver el hash de la clase persistente satisface el contrato de
 * {@link Object#hashCode()} (dos objetos iguales según {@link #equals(Object)}
 * comparten siempre la misma clase persistente, luego el mismo hash) y elimina
 * por completo esa clase de error. El costo es que todas las instancias de una
 * misma entidad colisionan en el mismo bucket, lo cual es irrelevante para el
 * tamaño habitual de una colección gestionada por el contexto de persistencia.
 * No use esta clase base como llave en estructuras de datos de alto rendimiento
 * con miles de elementos en memoria.</p>
 *
 * <p>
 * <strong>Recomendaciones de uso:</strong></p>
 * <ul>
 * <li>Extienda esta clase para todas las entidades que necesiten un
 * identificador numérico generado automáticamente.</li>
 * <li>No modifique el valor del {@code id} después de la persistencia, ya que
 * eso rompería el contrato de igualdad en colecciones y sesiones de
 * Hibernate.</li>
 * <li>Utilice el método {@link #getEntityClassName()} para logging o mensajes
 * de auditoría, ya que devuelve el nombre simple de la clase persistente aun
 * cuando la instancia sea un proxy.</li>
 * </ul>
 *
 * <p>
 * <strong>Manejo de proxies de Hibernate:</strong></p>
 * <p>
 * La resolución de la clase real se delega en {@link EntityUtils}, que consulta
 * los metadatos del inicializador perezoso sin forzar la materialización del
 * proxy. Consulte esa clase para el detalle de la técnica y sus limitaciones en
 * jerarquías de herencia de entidades.</p>
 * <p>
 * Por la misma razón, {@link #equals(Object)} y {@link #toString()} acceden al
 * identificador a través de {@link #getId()} y nunca por lectura directa del
 * campo. Un proxy no inicializado hereda el campo {@code id} pero lo mantiene
 * en {@code null}: solo el getter está interceptado por Hibernate y devuelve el
 * identificador real sin materializar la entidad. Leer el campo haría que una
 * entidad y su propio proxy resultaran distintas.</p>
 *
 * <p>
 * <strong>Auditoría temporal:</strong></p>
 * <p>
 * Los campos {@code created} y {@code updated} se calculan en UTC mediante el
 * reloj devuelto por {@link #clock()}. El uso de UTC evita que el mismo
 * registro obtenga offsets distintos según la zona horaria configurada en cada
 * nodo del clúster o en cada contenedor. Para pruebas deterministas,
 * sobrescriba {@link #clock()} devolviendo un {@code Clock.fixed(...)}.</p>
 *
 * <p>
 * <strong>Nota sobre claves asignadas:</strong> las entidades cuya clave es un
 * código de negocio asignado por la aplicación (sin {@code @GeneratedValue})
 * <em>no</em> deben extender esta clase, porque su contrato asume un
 * identificador nulo mientras la entidad es transiente. Véase {@code Periodo}
 * como referencia de ese caso.</p>
 *
 * @author Sebastián Salazar Molina.
 * @since 1.0.0
 * @version 2.1.0.26233
 * @see EntityUtils
 * @see jakarta.persistence.Id
 * @see jakarta.persistence.GeneratedValue
 */
@MappedSuperclass
public abstract class PkEntityBase implements Serializable {

    /**
     * El tiempo de ejecución de serialización asocia con cada clase
     * serializable un número de versión, llamado serialVersionUID, que se usa
     * durante la deserialización para verificar que el remitente y el receptor
     * de un objeto serializado hayan cargado clases para ese objeto que sean
     * compatibles con respecto a la serialización. Si el receptor ha cargado
     * una clase para el objeto que tiene un serialVersionUID diferente al de la
     * clase del remitente correspondiente, entonces la deserialización
     * resultará en una InvalidClassException.
     *
     * Una clase serializable declara su propio serialVersionUID como un campo
     * serialVersionUID que DEBE ser static, final, y de tipo long. Sino se
     * declara la JVM lo calcula en tiempo de ejecución.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Llave primaria Autoincremental
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk", nullable = false, updatable = false)
    private Long id;

    /**
     * Fecha de creación del registro, en UTC. Asignada una única vez por
     * {@link #onCreate()} y no actualizable en base de datos.
     */
    @Column(name = "created", nullable = false, updatable = false)
    private OffsetDateTime created;

    /**
     * Fecha de la última modificación del registro, en UTC. Asignada por
     * {@link #onCreate()} y refrescada por {@link #onUpdate()}.
     */
    @Column(name = "updated", nullable = false)
    private OffsetDateTime updated;

    /**
     * Callback JPA ejecutado antes de insertar una entidad por primera vez.
     *
     * <p>
     * Sincroniza {@code created} y {@code updated} con la fecha actual en
     * UTC.</p>
     *
     * <p>
     * <strong>Advertencia para subclases:</strong> si una subclase necesita su
     * propio callback {@code @PrePersist}, debe declararlo con un nombre
     * <em>distinto</em>. JPA invoca los callbacks de la superclase antes que
     * los de la subclase, pero un método con la misma firma sobrescribe a este
     * y la auditoría dejaría de asignarse.</p>
     */
    @PrePersist
    protected void onCreate() {
        this.created = AuditClock.now();
        this.updated = this.created;
    }

    /**
     * Callback JPA ejecutado antes de actualizar una entidad existente.
     *
     * <p>
     * <strong>Limitación conocida:</strong> los callbacks del ciclo de vida no
     * se disparan en sentencias masivas ({@code UPDATE} de JPQL, Criteria
     * update o SQL nativo). En esos casos el campo {@code updated} debe
     * actualizarse explícitamente dentro de la propia sentencia.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.updated = AuditClock.now();
    }

    /**
     * @return El identificador generado por el motor de base de datos.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id Fuerza un identificador, no necesariamente se podrá persistir
     * dependerá del mecanismo interno.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Fecha de creación, en UTC.
     */
    public OffsetDateTime getCreated() {
        return created;
    }

    /**
     * Establece la fecha de creación.
     *
     * <p>
     * Visibilidad restringida de forma deliberada: la columna está mapeada como
     * {@code updatable = false}, de modo que cualquier cambio posterior al
     * primer {@code persist} es ignorado por el motor y solo generaría una
     * discrepancia entre el objeto en memoria y la fila en base de datos. Se
     * mantiene accesible para procesos de migración o carga inicial que
     * requieran fijar el valor antes de persistir.</p>
     *
     * @param created Fecha de creación, en UTC.
     */
    protected void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    /**
     * @return Fecha de última actualización, en UTC.
     */
    public OffsetDateTime getUpdated() {
        return updated;
    }

    /**
     * Establece la fecha de última actualización.
     *
     * <p>
     * Visibilidad restringida de forma deliberada: {@link #onUpdate()}
     * sobrescribe este valor en cada {@code flush}, por lo que asignarlo desde
     * fuera de la jerarquía de entidades no tiene efecto observable.</p>
     *
     * @param updated Fecha de última actualización, en UTC.
     */
    protected void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    /**
     * Devuelve el nombre simple de la clase persistente de la entidad.
     *
     * <p>
     * Este método es útil para logging estructurado, auditoría o construcción
     * dinámica de consultas. Si la instancia es un proxy de Hibernate, devuelve
     * igualmente el nombre de la entidad y no el nombre generado del proxy (por
     * ejemplo {@code "Usuario"} en lugar de
     * {@code "Usuario$HibernateProxy$aB3xK"}).</p>
     *
     * <p>
     * Está anotado con {@code @JsonIgnore} porque sigue la convención de bean
     * property: sin la anotación, Jackson lo publicaría como un campo
     * {@code entityClassName} en cualquier respuesta que serializara la entidad
     * directamente.</p>
     *
     * @return el nombre de la clase persistente (ej. {@code "Usuario"},
     *         {@code "Transaccion"}).
     */
    @JsonIgnore
    public String getEntityClassName() {
        return EntityUtils.persistentClass(this).getSimpleName();
    }

    /**
     * Compara esta entidad con otro objeto para determinar si son iguales.
     *
     * <p>
     * El contrato implementado es el siguiente:</p>
     * <ol>
     * <li>Si {@code obj} es la misma referencia, devuelve {@code true}.</li>
     * <li>Si {@code obj} es {@code null}, devuelve {@code false}.</li>
     * <li>Si {@code obj} no es una {@link PkEntityBase}, devuelve
     * {@code false}.</li>
     * <li>Si las clases persistentes (considerando proxies de Hibernate) son
     * diferentes, devuelve {@code false}.</li>
     * <li>Si cualquiera de los dos identificadores es {@code null}, devuelve
     * {@code false}.</li>
     * <li>En caso contrario, compara ambos identificadores.</li>
     * </ol>
     *
     * <p>
     * <strong>Nota:</strong> para entidades transientes (con
     * {@code id == null}), este método siempre devuelve {@code false} al
     * comparar con cualquier otra instancia distinta de sí misma. Es una
     * decisión deliberada para evitar que dos objetos no persistidos y sin
     * identidad asignada se consideren iguales.</p>
     *
     * @param obj el objeto con el que comparar
     * @return {@code true} si las entidades son iguales según el contrato,
     * {@code false} en caso contrario
     * @see #hashCode()
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PkEntityBase other)) {
            return false;
        }
        if (!EntityUtils.samePersistentClass(this, other)) {
            return false;
        }
        final Long thisId = this.getId();
        final Long otherId = other.getId();
        if (thisId == null || otherId == null) {
            return false;
        }
        return thisId.equals(otherId);
    }

    /**
     * Calcula el código hash de la entidad a partir de su clase persistente.
     *
     * <p>
     * El valor devuelto es constante durante el ciclo de vida de la instancia,
     * incluso cuando el identificador pasa de {@code null} a un valor asignado
     * por la base de datos. Esto garantiza que una entidad agregada a un
     * {@code HashSet} antes de ser persistida siga siendo localizable después
     * del {@code persist}.</p>
     *
     * <p>
     * El contrato con {@link #equals(Object)} se mantiene: dos entidades
     * iguales comparten necesariamente la misma clase persistente y, por lo
     * tanto, el mismo hash.</p>
     *
     * @return el código hash de la clase persistente de la entidad
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return EntityUtils.persistentClass(this).hashCode();
    }

    /**
     * Devuelve una representación en cadena de la entidad, consistente en el
     * nombre de la clase persistente seguido del identificador entre corchetes.
     *
     * <p>
     * Ejemplo: {@code "Usuario[123]"} para una instancia de la clase
     * {@code Usuario} con id 123, o {@code "Usuario[null]"} si la entidad aún
     * es transiente.</p>
     *
     * @return una cadena con el formato {@code ClassName[id]}
     */
    @Override
    public String toString() {
        return this.getEntityClassName() + "[" + this.getId() + "]";
    }
}
