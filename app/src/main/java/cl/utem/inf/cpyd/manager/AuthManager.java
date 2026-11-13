package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.exceptions.ForbiddenException;
import cl.utem.inf.cpyd.exceptions.UtemException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import cl.utem.inf.cpyd.manager.helper.CacheHelper;
import cl.utem.inf.cpyd.utils.JwtUtils;
import cl.utem.inf.cpyd.utils.TextUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación y autorización de peticiones. Tiene tres
 * responsabilidades:
 *
 * <ol>
 * <li>Verificar que un correo pertenezca a una credencial activa.</li>
 * <li>Comparar API keys en tiempo constante.</li>
 * <li>Validar el JWT de cada petición de datos: firma, expiración, existencia y
 * estado de la credencial.</li>
 * </ol>
 *
 * <p>Las credenciales se obtienen a través de {@link CacheHelper}, de modo que las
 * peticiones no consultan la base de datos cada vez.</p>
 */
@Service
public class AuthManager {

    private final String sign;
    private final CacheHelper cacheHelper;

    /**
     * Mensaje genérico de credencial inválida. Es el mismo en todos los casos para no
     * revelar a un atacante qué parte falló.
     */
    private static final String BAD_CREDENTIAL = "Credenciales inválidas";

    /**
     * Crea el servicio con la clave de firma y el acceso cacheado a credenciales.
     *
     * @param cpydSign clave de firma de los JWT (propiedad {@code cpyd.sign}, normalmente
     * tomada de la variable {@code CPYD_SIGN}). Se recortan los espacios laterales.
     * @param cacheHelper acceso cacheado a credenciales.
     */
    public AuthManager(@Value("${cpyd.sign}") String cpydSign, final CacheHelper cacheHelper) {
        this.sign = StringUtils.trimToEmpty(cpydSign);
        this.cacheHelper = cacheHelper;
    }

    /**
     * Devuelve la clave de firma de los JWT.
     *
     * <p>Se expone solo para que {@link cl.utem.inf.cpyd.api.v1.LoginRest} firme los tokens. No debe registrarse
     * en logs ni enviarse en respuestas.</p>
     *
     * @return la clave, sin espacios laterales; cadena vacía si no está configurada.
     */
    public String getSign() {
        return sign;
    }

    /**
     * Obtiene la credencial asociada a un correo, a través de la caché.
     *
     * @param email correo; debe estar en minúsculas.
     * @return la credencial si existe; vacío en otro caso.
     */
    public Optional<Credential> getCredential(final String email) {
        return cacheHelper.getCredentialByEmail(email);
    }

    /**
     * Indica si un correo corresponde a una credencial activa.
     *
     * <p>La comparación es exacta: el conjunto de la caché contiene correos en minúsculas,
     * así que el llamador debe normalizar el correo antes (ver
     * {@link TextUtils#lower(String)}).</p>
     *
     * @param email correo a verificar.
     * @return {@code true} si está activo; {@code false} si es nulo, vacío o no figura
     * en el conjunto.
     */
    public boolean isValid(final String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }

        final Set<String> activeEmails = cacheHelper.getActiveEmails();
        if (CollectionUtils.isEmpty(activeEmails)) {
            return false;
        }

        return activeEmails.contains(email);
    }

    /**
     * Compara la API key recibida con la almacenada, usando {@link MessageDigest#isEqual}.
     *
     * <p>El tiempo de comparación no depende de en qué posición aparece el primer
     * carácter distinto, lo que evita deducir la clave midiendo respuestas (ataque de
     * tiempo). Como limitación, la longitud sí puede deducirse. La clave almacenada sigue
     * en texto plano en la base de datos.</p>
     *
     * @param credential credencial de referencia; si es {@code null}, devuelve {@code false}.
     * @param apiKey clave enviada por el cliente; si es vacía, devuelve {@code false}.
     * @return {@code true} si ambas claves son idénticas.
     */
    public boolean isValidApiKey(final Credential credential, final String apiKey) {
        if (credential == null) {
            return false;
        }

        if (StringUtils.isBlank(apiKey)) {
            return false;
        }

        // Comparación en tiempo constante para no filtrar información por tiempos de respuesta
        final byte[] expected = StringUtils.defaultString(credential.getApiKey()).getBytes(StandardCharsets.UTF_8);
        final byte[] given = apiKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, given);
    }

    /**
     * Valida la cabecera {@code Authorization} de una petición de datos.
     *
     * <p>Pasos, en orden:</p>
     * <ol>
     * <li>La cabecera existe y se quita el prefijo {@code Bearer}.</li>
     * <li>La firma del JWT es válida.</li>
     * <li>El JWT no ha expirado.</li>
     * <li>El claim {@code tokenId} existe y es un UUID.</li>
     * <li>Existe una credencial con ese token.</li>
     * <li>La credencial está activa.</li>
     * <li>El correo de la credencial coincide con el {@code sub} del JWT.</li>
     * </ol>
     *
     * <p>Cualquier fallo lanza {@link ForbiddenException}, que se traduce a HTTP 403.</p>
     *
     * @param authorizationHeader valor completo de la cabecera, por ejemplo
     * {@code Bearer eyJ...}.
     * @throws ForbiddenException si la validación falla por cualquiera de los motivos.
     */
    public void validate(final String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            throw new ForbiddenException();
        }

        final String jwt = StringUtils.trimToEmpty(Strings.CI.removeStart(StringUtils.trimToEmpty(authorizationHeader), "Bearer"));
        final JWTClaimsSet claims = JwtUtils.getValidClaims(sign, jwt);

        try {
            final String tokenStr = claims.getClaimAsString(JwtUtils.CLAIM_TOKEN_ID);
            if (StringUtils.isBlank(tokenStr)) {
                throw new ValidationException(BAD_CREDENTIAL);
            }
            final UUID token = UUID.fromString(tokenStr);

            Optional<Credential> opt = cacheHelper.getCredentialByToken(token);
            if (opt.isEmpty()) {
                throw new ValidationException(BAD_CREDENTIAL);
            }

            final Credential credential = opt.get();
            if (!credential.isActive()) {
                throw new UtemException(BAD_CREDENTIAL);
            }
            final String credEmail = TextUtils.lower(credential.getEmail());
            final String jwtEmail = TextUtils.lower(claims.getSubject());
            if (!Strings.CS.equals(credEmail, jwtEmail)) {
                throw new UtemException(BAD_CREDENTIAL);
            }
        } catch (UtemException | ValidationException | ParseException | IllegalArgumentException e) {
            throw new ForbiddenException("No valida el JWT", e);
        }
    }
}
