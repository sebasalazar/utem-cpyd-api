package cl.utem.inf.cpyd.api;

import cl.utem.inf.cpyd.exceptions.AuthException;
import cl.utem.inf.cpyd.exceptions.ForbiddenException;
import cl.utem.inf.cpyd.exceptions.NoDataException;
import cl.utem.inf.cpyd.exceptions.TooManyRequestsException;
import cl.utem.inf.cpyd.exceptions.UtemException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce excepciones de la aplicación y del framework en respuestas HTTP con
 * formato RFC 7807 ({@link org.springframework.http.ProblemDetail}).
 *
 * <p>Centraliza en un solo lugar la decisión de qué código HTTP corresponde a cada
 * error. Los controladores lanzan excepciones con significado de negocio y no
 * deciden el código de estado; así la política de errores es fácil de auditar y de
 * modificar.</p>
 *
 * <p>Cada respuesta incluye {@code type}, {@code title}, {@code status},
 * {@code detail}, {@code instance} (ruta solicitada), {@code timestamp},
 * {@code method} y, si existe, {@code query}.</p>
 *
 * <p>Por seguridad, los errores no previstos (HTTP 500) devuelven un mensaje
 * genérico y no el detalle interno de la excepción.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Construye el cuerpo de error estándar a partir del estado y del detalle.
     *
     * @param request petición HTTP original; puede ser {@code null}.
     * @param status código HTTP de la respuesta.
     * @param detail descripción del error destinada al cliente.
     * @return un {@link org.springframework.http.ProblemDetail} enriquecido con el tipo,
     * el título, la marca de tiempo, el método y la ruta.
     */
    private ProblemDetail makeProblemDetail(HttpServletRequest request, HttpStatus status, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);

        // Enriquecer
        pd.setType(URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/" + status.value()));
        pd.setTitle(status.getReasonPhrase());
        pd.setProperty("timestamp", Instant.now());

        if (request != null) {
            final String requestURI = request.getRequestURI();
            if (StringUtils.isNotBlank(requestURI)) {
                pd.setInstance(URI.create(requestURI));
            }

            pd.setProperty("method", request.getMethod());

            final String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString)) {
                pd.setProperty("query", queryString);
            }
        }

        return pd;
    }

    /**
     * Responde HTTP 401 cuando la autenticación falla.
     *
     * @param request petición HTTP original.
     * @param ae excepción de autenticación capturada.
     * @return respuesta 401 con el cuerpo de error.
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ProblemDetail> handleAuthException(HttpServletRequest request, AuthException ae) {
        LOGGER.error("Ocurrió un error de autenticación", ae);

        final HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail problem = makeProblemDetail(request, status, ae.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Responde HTTP 403 cuando el acceso a un recurso protegido es denegado.
     *
     * @param request petición HTTP original.
     * @param ae excepción de permisos capturada.
     * @return respuesta 403 con el cuerpo de error.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> handleForbiddenException(HttpServletRequest request, ForbiddenException ae) {
        LOGGER.error("Ocurrió un error con los permisos", ae);

        final HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail problem = makeProblemDetail(request, status, ae.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Responde HTTP 400 cuando los datos de entrada no son válidos.
     *
     * @param request petición HTTP original.
     * @param ve excepción de validación capturada.
     * @return respuesta 400 con el cuerpo de error.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(HttpServletRequest request, ValidationException ve) {
        LOGGER.error("Ocurrió un error de validación", ve);

        final HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail problem = makeProblemDetail(request, status, ve.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Responde HTTP 422 para errores de negocio o de configuración que no encajan en otra
     * categoría.
     *
     * @param request petición HTTP original.
     * @param ue excepción capturada.
     * @return respuesta 422 con el cuerpo de error.
     */
    @ExceptionHandler(UtemException.class)
    public ResponseEntity<ProblemDetail> handleUtemException(HttpServletRequest request, UtemException ue) {
        LOGGER.error("Ocurrió un error esperado", ue);

        final HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;

        ProblemDetail problem = makeProblemDetail(request, status, ue.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Responde HTTP 404 cuando la consulta no tiene resultados.
     *
     * @param request petición HTTP original.
     * @param nde excepción capturada.
     * @return respuesta 404 con el cuerpo de error.
     */
    @ExceptionHandler(NoDataException.class)
    public ResponseEntity<ProblemDetail> handleNoDataException(HttpServletRequest request, NoDataException nde) {
        LOGGER.error("No hay datos para generar una respuesta", nde);

        final HttpStatus status = HttpStatus.NOT_FOUND;

        ProblemDetail problem = makeProblemDetail(request, status, nde.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Responde HTTP 429 cuando se superó el límite de intentos de autenticación.
     *
     * @param request petición HTTP original.
     * @param e excepción capturada.
     * @return respuesta 429 con el cuerpo de error.
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ProblemDetail> handleTooMany(HttpServletRequest request, TooManyRequestsException e) {
        LOGGER.warn("Demasiados intentos de autenticación");

        final HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail problem = makeProblemDetail(request, status, e.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Responde HTTP 400 cuando un parámetro no puede convertirse a su tipo, por ejemplo
     * una fecha mal escrita.
     *
     * <p>El mensaje indica solo el nombre del parámetro y no repite el valor recibido,
     * para no reflejar entrada del usuario en la respuesta.</p>
     *
     * @param request petición HTTP original.
     * @param e excepción de conversión capturada.
     * @return respuesta 400 con el cuerpo de error.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail problem = makeProblemDetail(request, status, "Valor inválido para el parámetro: " + e.getName());
        return ResponseEntity.status(status).body(problem);
    }

    /**
     * Captura cualquier excepción no prevista. Registra el seguimiento completo en el log
     * y responde HTTP 500 con un mensaje genérico.
     *
     * @param request petición HTTP original.
     * @param e excepción no controlada.
     * @return respuesta 500 con el cuerpo de error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(HttpServletRequest request, Exception e) {
        LOGGER.error("Ocurrió un error INESPERADO", e);

        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problem = makeProblemDetail(request, status, "Error interno del servidor");
        return ResponseEntity.status(status).body(problem);
    }
}
