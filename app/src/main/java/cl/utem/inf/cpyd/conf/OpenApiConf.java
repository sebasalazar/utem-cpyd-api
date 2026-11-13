package cl.utem.inf.cpyd.conf;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI que springdoc publica en
 * {@code /v3/api-docs} y muestra en Swagger UI.
 *
 * <p>
 * La versión que aparece en la documentación no está escrita a mano: se toma de
 * {@code META-INF/build-info.properties}, que genera el plugin
 * {@code spring-boot-maven-plugin} a partir de la versión del {@code pom.xml}. Así,
 * la versión de Swagger UI siempre coincide con la del artefacto desplegado.</p>
 *
 * <p>
 * Define además el esquema de seguridad {@link #SECURITY_SCHEME} (token Bearer en
 * formato JWT), que Swagger UI usa en el botón <em>Authorize</em> para enviar el
 * token en las peticiones de prueba.</p>
 */
@Configuration
public class OpenApiConf {

    /** Nombre del esquema de seguridad JWT, referenciado por los endpoints protegidos. */
    public static final String SECURITY_SCHEME = "bearerAuth";

    /** Tipo de contenido de los errores, según RFC 7807. */
    public static final String PROBLEM_JSON = "application/problem+json";

    /** Versión usada si no existe {@code build-info.properties} (por ejemplo, en el IDE sin construir). */
    static final String FALLBACK_VERSION = "desconocida";

    private static final String DESCRIPTION = """
            API para consultar el **tipo de cambio diario del dólar (USD)** de Chile, Colombia y Ecuador.
            Proyecto auxiliar del taller de Computación Paralela y Distribuida de la UTEM.

            ### Cómo usar esta documentación

            1. Ejecute **`POST /v1/login/authenticate`** con su correo y su API key (botón *Try it out*).
            2. Copie el valor de `jwt` de la respuesta.
            3. Pulse el botón **Authorize** (arriba a la derecha), escriba el token y confirme.
               No es necesario anteponer `Bearer`: Swagger UI lo agrega.
            4. Ejecute las consultas de **Tipos de cambio**. El token queda guardado en el navegador
               hasta que pulse *Logout*.

            El token (JWT) dura **67 minutos**. Al expirar, vuelva a iniciar sesión.

            ### Códigos de respuesta

            | Código | Significado |
            |---|---|
            | 200 | Consulta exitosa |
            | 400 | Parámetro inválido: país no soportado, fecha mal escrita o rango inválido |
            | 401 | Correo o API key incorrectos |
            | 403 | Token ausente, inválido, expirado, o credencial inexistente o inactiva |
            | 404 | No hay datos para la consulta (por ejemplo, fines de semana o feriados) |
            | 422 | Error de configuración del servidor |
            | 429 | Demasiados intentos fallidos de inicio de sesión (límite de 15 minutos) |

            Los errores siguen el formato **RFC 7807** (`application/problem+json`).

            ### Notas para estudiantes

            - Yahoo Finance solo publica datos en días hábiles: los fines de semana no tienen cotización.
            - Ecuador usa el dólar como moneda, por lo que su tasa siempre vale `1.00`.
            - Las consultas de rango admiten como máximo **3660 días** por petición.
            """;

    /**
     * Construye el documento OpenAPI con la información del proyecto, el esquema de
     * seguridad y las etiquetas de agrupación.
     *
     * @param buildProperties propiedades de construcción generadas por Maven; puede no
     * estar disponible si el proyecto no se construyó con Maven.
     * @return el objeto {@link OpenAPI} que springdoc combina con las rutas detectadas.
     */
    @Bean
    public OpenAPI cpydOpenApi(final ObjectProvider<BuildProperties> buildProperties) {
        final BuildProperties build = buildProperties.getIfAvailable();
        final String version = build != null ? build.getVersion() : FALLBACK_VERSION;

        return new OpenAPI()
                .info(new Info()
                        .title("Tasa del Dólar API")
                        .version(version)
                        .description(DESCRIPTION)
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtenido en `POST /v1/login/authenticate`. "
                                        + "Válido 67 minutos.")))
                .tags(List.of(
                        new Tag()
                                .name("Autenticación")
                                .description("Obtención del JWT a partir de correo y API key. Es el único endpoint público."),
                        new Tag()
                                .name("Tipos de cambio")
                                .description("Consulta de tipos de cambio diarios (1 USD) por país. Requiere JWT.")));
    }
}
