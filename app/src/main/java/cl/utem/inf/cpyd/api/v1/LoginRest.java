package cl.utem.inf.cpyd.api.v1;

import cl.utem.inf.cpyd.conf.OpenApiConf;
import cl.utem.inf.cpyd.domain.data.LoginRequest;
import cl.utem.inf.cpyd.domain.data.LoginResponse;
import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.exceptions.AuthException;
import cl.utem.inf.cpyd.manager.AuthManager;
import cl.utem.inf.cpyd.manager.LoginAttemptManager;
import cl.utem.inf.cpyd.utils.JwtUtils;
import cl.utem.inf.cpyd.utils.TextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación. Recibe un correo y una API key y, si son
 * válidos, devuelve un JWT que el cliente usará en las siguientes peticiones.
 * Ruta base {@code /v1/login}.
 *
 * <p>
 * Es el único endpoint que no exige token. Además de validar las credenciales,
 * aplica un límite de intentos fallidos por correo mediante
 * {@link LoginAttemptManager}.</p>
 */
@Tag(name = "Autenticación")
@RestController
@RequestMapping(value = {"/v1/login"})
public class LoginRest {

    private final AuthManager authManager;

    private final LoginAttemptManager attempts;

    /**
     * Crea el controlador. Spring inyecta las dependencias por constructor.
     *
     * @param authManager servicio de autenticación.
     * @param attempts contador de intentos fallidos por correo.
     */
    public LoginRest(final AuthManager authManager, final LoginAttemptManager attempts) {
        this.authManager = authManager;
        this.attempts = attempts;
    }

    /**
     * Autentica al usuario y emite un JWT.
     *
     * <p>
     * Orden de verificación:</p>
     * <ol>
     * <li>El correo no está bloqueado por intentos fallidos (si lo está, HTTP
     * 429).</li>
     * <li>El correo pertenece a una credencial activa (si no, HTTP 401).</li>
     * <li>Existe la credencial (si no, HTTP 401).</li>
     * <li>La API key coincide (si no, HTTP 401).</li>
     * </ol>
     *
     * <p>
     * Cada fallo, salvo el bloqueo, se registra en el contador. Un login
     * exitoso lo reinicia. Todos los fallos de autenticación devuelven el mismo
     * mensaje, para no revelar qué correos existen.</p>
     *
     * @param request cuerpo JSON con el correo y la API key.
     * @return {@code 200} con un {@link LoginResponse} que contiene el JWT.
     * @throws cl.utem.inf.cpyd.exceptions.AuthException si las credenciales no
     * son válidas.
     * @throws cl.utem.inf.cpyd.exceptions.TooManyRequestsException si se superó
     * el límite de intentos.
     */
    @Operation(
            summary = "Iniciar sesión y obtener un JWT",
            description = "Recibe el correo y la API key de un usuario activo y devuelve un JWT firmado (HS256), "
                    + "válido 67 minutos. Copie el valor de `jwt` y péguelo en **Authorize** para probar los "
                    + "endpoints de tipos de cambio. No requiere token. Tras 5 intentos fallidos en 15 minutos, "
                    + "el correo queda bloqueado temporalmente (429).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credenciales válidas: JWT emitido",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Cuerpo inválido: correo mal formado o campos vacíos",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "401", description = "Correo inexistente o inactivo, o API key incorrecta. "
                + "El mensaje es el mismo en todos los casos, a propósito",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "El servidor no tiene configurada la clave de firma (CPYD_SIGN)",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos para este correo",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(value = {"/authenticate"}, consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        final String email = TextUtils.lower(request.getEmail());
        attempts.checkAllowed(email);

        if (!authManager.isValid(email)) {
            attempts.recordFailure(email);
            throw new AuthException();
        }

        Optional<Credential> opt = authManager.getCredential(email);
        if (opt.isEmpty()) {
            attempts.recordFailure(email);
            throw new AuthException();
        }

        final Credential credential = opt.get();
        final String apiKey = StringUtils.trimToEmpty(request.getApiKey());

        if (!authManager.isValidApiKey(credential, apiKey)) {
            attempts.recordFailure(email);
            throw new AuthException();
        }

        attempts.reset(email);
        final String jwt = JwtUtils.makeJwt(authManager.getSign(), credential.getEmail(), credential.getToken());
        return ResponseEntity.ok(new LoginResponse(jwt));
    }
}
