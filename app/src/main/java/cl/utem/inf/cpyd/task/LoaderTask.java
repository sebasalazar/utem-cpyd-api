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
        try {
            File file = new File(filePath);
            if (file.isFile() && file.canRead()) {
                loaderService.load(file);
            } else {
                LOGGER.error("No se ha encontrado el archivo {}", filePath);
            }
        } catch (Exception e) {
            LOGGER.error("Error al cargar datos en el sistema", e);
        }
    }

    @Scheduled(fixedDelayString = "${scheduler.once.task}")
    public void runOnce() {
        loadData();
    }
}
