package cl.utem.inf.cpyd;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Permite desplegar la aplicación como archivo WAR en un contenedor de servlets
 * externo, por ejemplo Apache Tomcat.
 *
 * <p>Con {@code ./mvnw spring-boot:run} no se usa esta clase: el servidor embebido
 * arranca mediante {@link CpydApplication#main(String[])}. Su función solo se
 * activa en el despliegue tradicional, donde el contenedor invoca a Spring a
 * través de la interfaz de Servlet 3.0 o superior.</p>
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * Indica a Spring cuál es la clase de configuración principal cuando el contenedor
     * inicia la aplicación.
     *
     * @param application constructor de la aplicación Spring, provisto por el contenedor.
     * @return el mismo constructor, con {@link CpydApplication} registrada como fuente.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CpydApplication.class);
    }
}
