package cl.utem.inf.cpyd.domain.enums;

/**
 * Enumeración que representa los países de operación en la región andina.
 * <p>
 * Esta enumeración centraliza la información básica de cada país, incluyendo su
 * código internacional, su name oficial y su currency de curso legal. Su
 * propósito es evitar valores "mágicos" (hardcoded strings) en la lógica de
 * negocio, garantizando consistencia y facilitando el mantenimiento.
 * </p>
 * <p>
 * <b>Glosario para no técnicos:</b>
 * <ul>
 * <li><b>Código (2 letras):</b> Estándar internacional (ISO 3166-1 alpha-2)
 * utilizado globalmente para identificar países de forma inequívoca (ej. "CL"
 * para Chile).</li>
 * <li><b>Moneda (ISO 4217):</b> Código internacional de tres letras que
 * representa la divisa oficial del país (ej. "CLP" para Peso Chileno).</li>
 * </ul>
 */
public enum Country {

    // El id es el valor persistido (smallint) y no debe cambiar aunque se reordene el enum.
    /** Chile: código CL, moneda peso chileno (CLP). */
    CHILE(0, "CL", "Chile", "CLP"),
    /** Colombia: código CO, moneda peso colombiano (COP). */
    COLOMBIA(1, "CO", "Colombia", "COP"),
    /** Ecuador: código EC, moneda dólar estadounidense (USD); su tasa siempre vale 1. */
    ECUADOR(2, "EC", "Ecuador", "USD");

    // Los atributos se declaran como final para garantizar inmutabilidad.
    private final int id;
    private final String code;
    private final String name;
    private final String currency;

    /**
     * Constructor de la enumeración.
     *
     * @param code Código de país de dos letras (ISO 3166-1 alpha-2).
     * @param name Nombre completo y oficial del país.
     * @param currency Código de la currency de curso legal (ISO 4217).
     */
    Country(final int id, final String code, final String name, final String currency) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.currency = currency;
    }

    /**
     * Obtiene el código de dos letras del país.
     *
     * @return El código ISO 3166-1 alpha-2 como una cadena de texto.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Busca un país por el id con que se persiste en la base de datos.
     *
     * @param id valor almacenado en la columna: 0, 1 o 2.
     * @return el país correspondiente.
     * @throws IllegalArgumentException si el id no corresponde a ningún país.
     */
    public static Country fromId(final int id) {
        for (Country c : values()) {
            if (c.id == id) {
                return c;
            }
        }
        throw new IllegalArgumentException("Id de país desconocido: " + id);
    }

    /**
     * Devuelve el código de dos letras del país, según ISO 3166-1 alfa-2.
     *
     * @return {@code CL}, {@code CO} o {@code EC}.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Obtiene el name del país.
     *
     * @return El name completo del país como una cadena de texto.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtiene el código de la currency del país.
     *
     * @return El código ISO 4217 de la currency como una cadena de texto.
     */
    public String getCurrency() {
        return this.currency;
    }
}
