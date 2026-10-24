package cl.utem.inf.cpyd.task;

import cl.utem.inf.cpyd.service.LoaderService;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoaderTask {

    private final String filePath;
    private final LoaderService loaderService;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoaderTask.class);

    public LoaderTask(@Value("${exchange.file}") String filePathStr, LoaderService loaderService) {
        this.filePath = StringUtils.trimToEmpty(filePathStr);
        this.loaderService = loaderService;
    }

    private void loadData() {
        if (StringUtils.isBlank(filePath)) {
            LOGGER.warn("No hay ruta al archivo de carga");
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.isFile()) {
                LOGGER.error("La ruta {} no es un archivo", filePath);
                return;
            }

            if (!file.canRead()) {
                LOGGER.error("El archivo {} no se puede leer por falta de permisos", filePath);
                return;
            }

            loaderService.load(file);
        } catch (Exception e) {
            LOGGER.error("Error al cargar datos en el sistema", e);
        }
    }

    @Scheduled(fixedDelayString = "${scheduler.once.task}")
    public void runOnce() {
        LOGGER.info("=== Iniciando  la carga de datos ===");
        loadData();
        LOGGER.info("=== Terminando la carga de datos ===");
    }
}
