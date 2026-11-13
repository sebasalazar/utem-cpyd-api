package cl.utem.inf.cpyd;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.util.Json;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Verifica la documentación OpenAPI sobre el servidor real: la versión que se publica
 * debe ser la del proyecto, todos los endpoints deben estar documentados y Swagger UI
 * debe servirse bajo el context path.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocsTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectProvider<BuildProperties> buildProperties;

    private HttpResponse<String> get(String path) throws Exception {
        final HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode apiDocs() throws Exception {
        final HttpResponse<String> res = get("/cpyd/v3/api-docs");
        assertEquals(200, res.statusCode());
        return Json.mapper().readTree(res.body());
    }

    @Test
    void versionComesFromProjectBuild() throws Exception {
        final BuildProperties build = buildProperties.getIfAvailable();
        assertNotNull(build, "build-info.properties debe generarse con Maven");
        assertEquals(build.getVersion(), apiDocs().at("/info/version").asText());
    }

    @Test
    void documentsAllEndpoints() throws Exception {
        final JsonNode paths = apiDocs().at("/paths");
        assertTrue(paths.has("/v1/login/authenticate"));
        assertTrue(paths.has("/v1/exchanges/{country}"));
        assertTrue(paths.has("/v1/exchanges/{country}/{exchange_date}"));
        assertEquals("Iniciar sesión y obtener un JWT", paths.at("/~1v1~1login~1authenticate/post/summary").asText());
    }

    @Test
    void protectedEndpointsRequireBearerAndLoginDoesNot() throws Exception {
        final JsonNode root = apiDocs();
        assertEquals("http", root.at("/components/securitySchemes/bearerAuth/type").asText());
        assertEquals("bearer", root.at("/components/securitySchemes/bearerAuth/scheme").asText());

        assertTrue(root.at("/paths/~1v1~1exchanges~1{country}/get/security").toString().contains("bearerAuth"));
        assertTrue(root.at("/paths/~1v1~1exchanges~1{country}~1{exchange_date}/get/security").toString().contains("bearerAuth"));
        assertTrue(root.at("/paths/~1v1~1login~1authenticate/post/security").isMissingNode());
    }

    @Test
    void authorizationHeaderIsNotDuplicatedAsParameter() throws Exception {
        final JsonNode params = apiDocs().at("/paths/~1v1~1exchanges~1{country}/get/parameters");
        for (JsonNode p : params) {
            assertFalse("Authorization".equals(p.path("name").asText()), "El header debe quedar oculto");
        }
    }

    @Test
    void errorResponsesUseProblemJson() throws Exception {
        final JsonNode notFound = apiDocs().at("/paths/~1v1~1exchanges~1{country}~1{exchange_date}/get/responses/404");
        assertTrue(notFound.at("/content").has("application/problem+json"));
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        final HttpResponse<String> res = get("/cpyd/swagger-ui/index.html");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("swagger-ui"));
    }
}
