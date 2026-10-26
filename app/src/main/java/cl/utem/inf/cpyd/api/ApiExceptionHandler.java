package cl.utem.inf.cpyd.api;

import cl.utem.inf.cpyd.exceptions.AuthException;
import cl.utem.inf.cpyd.exceptions.NoDataException;
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

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

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

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(HttpServletRequest request, AuthException ae) {
        LOGGER.error("Ocurrió un error de autenticación", ae);

        final HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail problem = makeProblemDetail(request, status, ae.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(HttpServletRequest request, ValidationException ve) {
        LOGGER.error("Ocurrió un error de validación", ve);

        final HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail problem = makeProblemDetail(request, status, ve.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(UtemException.class)
    public ResponseEntity<ProblemDetail> handleUtemException(HttpServletRequest request, UtemException ue) {
        LOGGER.error("Ocurrió un error esperado", ue);

        final HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT;

        ProblemDetail problem = makeProblemDetail(request, status, ue.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(NoDataException.class)
    public ResponseEntity<ProblemDetail> handleUtemException(HttpServletRequest request, NoDataException nde) {
        LOGGER.error("No hay datos para generar una respuesta", nde);

        final HttpStatus status = HttpStatus.NOT_FOUND;

        ProblemDetail problem = makeProblemDetail(request, status, nde.getMessage());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(HttpServletRequest request, Exception e) {
        LOGGER.error("Ocurrió un error INESPERADO", e);

        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problem = makeProblemDetail(request, status, e.getMessage());
        return ResponseEntity.status(status).body(problem);
    }
}
