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
 * </p>
 */
public enum Country {

    CHILE("CL", "Chile", "CLP"),
    COLOMBIA("CO", "Colombia", "COP"),
    ECUADOR("EC", "Ecuador", "USD");

    // Los atributos se declaran como final para garantizar inmutabilidad.
    private final String code;
    private final String name;
    private final String currency;

    /**
     * Constructor de la enumeración.
     *
     * @param codigo Código de país de dos letras (ISO 3166-1 alpha-2).
     * @param name Nombre completo y oficial del país.
     * @param currency Código de la currency de curso legal (ISO 4217).
     */
    Country(final String codigo, final String name, final String currency) {
        this.code = codigo;
        this.name = name;
        this.currency = currency;
    }

    /**
     * Obtiene el código de dos letras del país.
     *
     * @return El código ISO 3166-1 alpha-2 como una cadena de texto.
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
