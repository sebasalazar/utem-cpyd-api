package cl.utem.inf.cpyd.service;

import cl.utem.inf.cpyd.domain.enums.Country;
import cl.utem.inf.cpyd.task.helper.ExchangeHelper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoaderService {

    private final ExchangeHelper exchangeHelper;
    private static final String[] CSV_HEADER = {"País", "Fecha", "Cambio_USD"};
    private static final Logger LOGGER = LoggerFactory.getLogger(LoaderService.class);

    public LoaderService(ExchangeHelper exchangeHelper) {
        this.exchangeHelper = exchangeHelper;
    }

    private static void validarEncabezado(final String[] header) {
        if (header == null || !Arrays.equals(header, CSV_HEADER)) {
            final String message = String.format("Encabezado inválido. Se esperaba: %s. Se recibió: %s",
                    String.join(",", CSV_HEADER), Arrays.toString(header));
            throw new IllegalArgumentException(message);
        }
    }

    private static ExchangeHelper.CsvRow convertirFila(final String[] row, final long line) {
        if (row.length != CSV_HEADER.length) {
            final String message = String.format("La línea %d tiene %d columnas; se esperaban %d", line, row.length, CSV_HEADER.length);
            throw new IllegalArgumentException(message);
        }

        final String countryName = StringUtils.upperCase(StringUtils.trimToEmpty(row[0]));
        if (StringUtils.isBlank(countryName)) {
            throw new IllegalArgumentException("La línea " + line + " no contiene país");
        }

        // LocalDate.parse exige formato ISO_LOCAL_DATE: yyyy-MM-dd.
        final LocalDate date = LocalDate.parse(StringUtils.trimToEmpty(row[1]));

        // Constructor con String: no pasa por coma flotante.
        final BigDecimal cambioUsd = new BigDecimal(StringUtils.trimToEmpty(row[2]));

        return new ExchangeHelper.CsvRow(Country.valueOf(countryName), date, cambioUsd);
    }

    public void load(File file) {
        try (final Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8); final CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .build())
                .build()) {
            LOGGER.debug("Cargando el archivo {}", file.getAbsolutePath());
            final String[] encabezado = csvReader.readNext();
            validarEncabezado(encabezado);

            String[] fila;
            final List<ExchangeHelper.CsvRow> list = new ArrayList<>();
            while ((fila = csvReader.readNext()) != null) {
                // OpenCSV puede devolver un array vacío ante líneas en blanco.
                if (ArrayUtils.isEmpty(fila)) {
                    LOGGER.error("No hay datos en fila");
                    continue;
                }

                final long lineaFisica = csvReader.getLinesRead();
                LOGGER.debug("Cargando la línea {}", lineaFisica);

                list.add(convertirFila(fila, lineaFisica));
            }
            exchangeHelper.saveAll(list);
        } catch (Exception e) {
            LOGGER.error("Error al cargar archivo", e);
        }
    }
}
