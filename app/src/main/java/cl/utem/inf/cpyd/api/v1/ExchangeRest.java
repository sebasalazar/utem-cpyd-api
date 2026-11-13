package cl.utem.inf.cpyd.api.v1;

import cl.utem.inf.cpyd.conf.OpenApiConf;
import cl.utem.inf.cpyd.domain.data.ExchangeData;
import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.domain.model.Exchange;
import cl.utem.inf.cpyd.exceptions.NoDataException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import cl.utem.inf.cpyd.manager.AuthManager;
import cl.utem.inf.cpyd.manager.ExchangeManager;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los tipos de cambio por país y fecha, bajo la ruta
 * {@code /v1/exchanges}.
 *
 * <p>Ambos endpoints siguen el mismo patrón, que conviene repetir en cualquier
 * endpoint nuevo:</p>
 * <ol>
 * <li>Validar el JWT con {@link AuthManager#validate(String)}.</li>
 * <li>Convertir el país de la ruta en un {@link Country}.</li>
 * <li>Consultar mediante {@link ExchangeManager}.</li>
 * <li>Traducir cada entidad a un {@link ExchangeData}.</li>
 * </ol>
 *
 * <p>La autenticación no es automática: cada método la invoca de forma explícita.
 * Si se omite esa llamada, el endpoint queda abierto.</p>
 */
@Tag(name = "Tipos de cambio")
@RestController
@RequestMapping(value = "/v1/exchanges")
public class ExchangeRest {

    /**
     * Número máximo de días que puede abarcar una consulta de rango (3660, aproximadamente
     * diez años). Limita el volumen de datos devueltos, ya que la API no pagina.
     */
    private static final long MAX_RANGE_DAYS = 3660;

    private final AuthManager authManager;
    private final ExchangeManager exchangeManager;

    /**
     * Crea el controlador. Spring inyecta las dependencias por constructor.
     *
     * @param authManager servicio de validación de JWT.
     * @param exchangeManager servicio de consulta de tipos de cambio.
     */
    public ExchangeRest(final AuthManager authManager, final ExchangeManager exchangeManager) {
        this.authManager = authManager;
        this.exchangeManager = exchangeManager;
    }

    /**
     * Convierte el nombre de país recibido en la ruta al enum {@link Country}. La
     * comparación no distingue mayúsculas ni espacios laterales.
     *
     * @param countryStr nombre del país, por ejemplo {@code chile}.
     * @return el país correspondiente.
     * @throws ValidationException si el texto está vacío o no corresponde a un país
     * soportado.
     */
    private Country convert(final String countryStr) {
        if (StringUtils.isEmpty(countryStr)) {
            throw new ValidationException("El país es nulo");
        }

        final String value = StringUtils.upperCase(StringUtils.trimToEmpty(countryStr));
        switch (value) {
            case "CHILE" -> {
                return Country.CHILE;
            }
            case "COLOMBIA" -> {
                return Country.COLOMBIA;
            }
            case "ECUADOR" -> {
                return Country.ECUADOR;
            }
            default -> {
                final String logCountry = StringEscapeUtils.escapeJava(value);
                throw new ValidationException(String.format("El valor de país (%s) no está soportado", logCountry));
            }
        }
    }

    /**
     * Devuelve los tipos de cambio de un país entre dos fechas, ambas inclusive.
     * Corresponde a {@code GET /v1/exchanges/{country}?start_date=…&end_date=…}.
     *
     * <p>Valida que {@code startDate} no sea posterior a {@code endDate} y que el rango
     * no supere {@link #MAX_RANGE_DAYS} días. La consulta no define un orden explícito
     * de los resultados.</p>
     *
     * @param authorization cabecera {@code Authorization} con el JWT ({@code Bearer ...}).
     * @param countryStr país indicado en la ruta.
     * @param startDate fecha inicial, inclusive.
     * @param endDate fecha final, inclusive.
     * @return {@code 200} con la lista de cotizaciones encontradas.
     * @throws ValidationException si el rango es inválido o excede el máximo.
     * @throws NoDataException si no hay registros en el rango (HTTP 404).
     */
    @Operation(
            summary = "Consultar tipos de cambio por rango de fechas",
            description = "Devuelve una cotización por cada día con dato entre `start_date` y `end_date` "
                    + "(ambas inclusive). Los días sin mercado, como fines de semana y feriados, no aparecen "
                    + "en la lista. Requiere token Bearer.",
            security = @SecurityRequirement(name = OpenApiConf.SECURITY_SCHEME))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de cotizaciones del rango, una por día con dato",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = ExchangeData.class)))),
        @ApiResponse(responseCode = "400", description = "Parámetro inválido: país no soportado, fecha mal escrita, "
                + "start_date posterior a end_date, o rango mayor a 3660 días",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Token ausente, inválido, expirado o de una credencial inactiva",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "No hay datos para el país y el rango indicados",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado del servidor",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = {"/{country}"})
    public ResponseEntity<List<ExchangeData>> exchangeByCountryAndDate(
            @Parameter(hidden = true) @RequestHeader(name = "Authorization") String authorization,
            @Parameter(description = "País: chile, colombia o ecuador (sin distinguir mayúsculas)", required = true,
                    example = "chile", schema = @Schema(allowableValues = {"chile", "colombia", "ecuador"}))
            @PathVariable("country") String countryStr,
            @Parameter(description = "Fecha inicial, inclusive (formato yyyy-MM-dd)", required = true, example = "2025-01-01")
            @RequestParam("start_date") LocalDate startDate,
            @Parameter(description = "Fecha final, inclusive (formato yyyy-MM-dd)", required = true, example = "2025-01-31")
            @RequestParam("end_date") LocalDate endDate) {
        authManager.validate(authorization);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("start_date no puede ser posterior a end_date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > MAX_RANGE_DAYS) {
            throw new ValidationException("El rango máximo permitido es de " + MAX_RANGE_DAYS + " días");
        }

        final Country country = convert(countryStr);
        List<Exchange> exchanges = exchangeManager.getExchange(country, startDate, endDate);
        if (CollectionUtils.isEmpty(exchanges)) {
            throw new NoDataException("No hay datos para las fecha y el país indicado");
        }

        List<ExchangeData> data = new ArrayList<>();
        for (Exchange exchange : exchanges) {
            data.add(new ExchangeData(exchange));
        }

        return ResponseEntity.ok(data);
    }

    /**
     * Devuelve el tipo de cambio de un país para una fecha exacta. Corresponde a
     * {@code GET /v1/exchanges/{country}/{exchange_date}}.
     *
     * @param authorization cabecera {@code Authorization} con el JWT ({@code Bearer ...}).
     * @param countryStr país indicado en la ruta.
     * @param date fecha exacta de la cotización.
     * @return {@code 200} con la cotización.
     * @throws NoDataException si no existe registro para ese país y fecha (HTTP 404).
     */
    @Operation(
            summary = "Consultar el tipo de cambio de un día",
            description = "Devuelve la cotización de un país para una fecha exacta. Requiere token Bearer.",
            security = @SecurityRequirement(name = OpenApiConf.SECURITY_SCHEME))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cotización del país en la fecha indicada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeData.class))),
        @ApiResponse(responseCode = "400", description = "País no soportado o fecha mal escrita",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Token ausente, inválido, expirado o de una credencial inactiva",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "No existe cotización para ese país y fecha (por ejemplo, un fin de semana)",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado del servidor",
                content = @Content(mediaType = OpenApiConf.PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = {"/{country}/{exchange_date}"})
    public ResponseEntity<ExchangeData> exchangeByCountryAndDate(
            @Parameter(hidden = true) @RequestHeader(name = "Authorization") String authorization,
            @Parameter(description = "País: chile, colombia o ecuador (sin distinguir mayúsculas)", required = true,
                    example = "chile", schema = @Schema(allowableValues = {"chile", "colombia", "ecuador"}))
            @PathVariable("country") String countryStr,
            @Parameter(description = "Fecha de la cotización (formato yyyy-MM-dd)", required = true, example = "2025-01-02")
            @PathVariable("exchange_date") LocalDate date) {
        authManager.validate(authorization);

        final Country country = convert(countryStr);
        Optional<Exchange> optional = exchangeManager.getExchange(country, date);
        if (optional.isEmpty()) {
            throw new NoDataException("No hay datos para las fecha y el país indicado");
        }

        return ResponseEntity.ok(new ExchangeData(optional.get()));
    }
}
