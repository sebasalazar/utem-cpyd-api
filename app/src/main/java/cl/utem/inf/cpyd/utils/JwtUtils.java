package cl.utem.inf.cpyd.utils;

import cl.utem.inf.cpyd.exceptions.ForbiddenException;
import cl.utem.inf.cpyd.exceptions.UtemException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * Utilitaria para la generación y validación segura de JSON Web Tokens (JWT).
 *
 * <p>
 * Esta clase es inmutable y no instanciable. Todas sus operaciones son
 * deterministas y no mantienen estado interno.</p>
 *
 * @author Sebastián Salazar Molina
 * @email ssalazar@utem.cl
 * @see <a href="https://orcid.org/0009-0006-1498-5658">ORCID:
 * 0009-0006-1498-5658</a>
 * @institution Universidad Tecnológica Metropolitana (UTEM)
 * @version 0.0.2-SNAPSHOT
 * @since 0.0.1
 */
public final class JwtUtils {

    private static final String ISSUER = "api.sebastian.cl";
    /**
     * Nombre del claim que transporta el UUID de la credencial dentro del JWT.
     * Se usa un nombre descriptivo en lugar de una abreviatura para facilitar la lectura.
     */
    public static final String CLAIM_TOKEN_ID = "tokenId";

    // 67 minutos: duración deliberada para evitar colisiones con invalidaciones 
    // de caché basadas en ventanas horarias exactas de 60 minutos.
    private static final Duration JWT_DURATION = Duration.ofMinutes(67);

    // HS256 requiere una clave secreta de al menos 256 bits (32 bytes).
    private static final int MIN_SECRET_KEY_LENGTH_BYTES = 32;
    private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);

    private JwtUtils() {
        throw new UnsupportedOperationException("Clase utilitaria no instanciable");
    }

    /**
     * Genera un JWT firmado utilizando el algoritmo HS256.
     *
     * @param sign La clave secreta para la firma (mínimo 32 bytes en UTF-8).
     * @param subject El sujeto o identificador principal del token (ej. email o
     * ID de usuario).
     * @param token Un identificador único adicional (ej. UUID de sesión o
     * credencial).
     * @return La representación serializada (String) del JWT firmado.
     * @throws UtemException si la clave secreta es inválida o falla la firma
     * criptográfica.
     * @throws ValidationException si el token proporcionado es nulo.
     */
    public static String makeJwt(final String sign, final String subject, final UUID token) {
        if (StringUtils.isBlank(sign)) {
            throw new UtemException("La firma no puede estar vacía");
        }

        final byte[] secretBytes = sign.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_KEY_LENGTH_BYTES) {
            throw new UtemException("La longitud de la firma es insuficiente. Se requieren al menos 32 bytes.");
        }

        if (StringUtils.isBlank(subject)) {
            throw new UtemException("Se requiere un asunto (subject) previo a la firma");
        }

        if (token == null) {
            throw new ValidationException("Se requiere un token válido");
        }

        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plus(JWT_DURATION);

        try {
            final JWSSigner signer = new MACSigner(secretBytes);

            final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(UuidUtils.generateV7AsString())
                    .issuer(ISSUER)
                    .subject(subject)
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .claim(CLAIM_TOKEN_ID, token.toString())
                    .build();

            final SignedJWT signedJWT = new SignedJWT(JWT_HEADER, claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new UtemException("No fue posible generar criptográficamente el JWT", e);
        }
    }

    /**
     * Parsea y valida un JWT, verificando su firma y su fecha de expiración.
     *
     * @param sign La clave secreta utilizada para verificar la firma.
     * @param jwt La cadena de texto del JWT a validar.
     * @return El conjunto de claims validados si la firma y la expiración son
     * correctas.
     * @throws ForbiddenException si el JWT es nulo, está malformado, ha
     * expirado o su firma es inválida.
     */
    public static JWTClaimsSet getValidClaims(final String sign, final String jwt) {
        if (StringUtils.isBlank(sign)) {
            throw new ForbiddenException("La firma de validación es inválida o está vacía");
        }

        if (StringUtils.isBlank(jwt)) {
            throw new ForbiddenException("Se requiere un JWT válido para su procesamiento");
        }

        try {
            final byte[] secretBytes = sign.getBytes(StandardCharsets.UTF_8);
            final JWSVerifier verifier = new MACVerifier(secretBytes);

            final SignedJWT parsedJwt = SignedJWT.parse(jwt);

            // 1. Verificación Criptográfica Obligatoria
            if (!parsedJwt.verify(verifier)) {
                throw new ForbiddenException("La firma criptográfica del JWT no es válida");
            }

            final JWTClaimsSet claims = parsedJwt.getJWTClaimsSet();

            // 2. Verificación de Expiración Obligatoria
            final Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new ForbiddenException("El JWT ha expirado y no es válido");
            }

            return claims;
        } catch (ParseException e) {
            throw new ForbiddenException("El formato del JWT es estructuralmente inválido", e);
        } catch (JOSEException e) {
            throw new ForbiddenException("Error criptográfico al validar el JWT", e);
        }
    }
}
