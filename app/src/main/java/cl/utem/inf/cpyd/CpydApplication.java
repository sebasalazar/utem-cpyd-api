package cl.utem.inf.cpyd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Tasa del Dólar.
 *
 * <p>La anotación {@link org.springframework.boot.autoconfigure.SpringBootApplication}
 * combina tres funciones:</p>
 * <ul>
 * <li><b>Configuración:</b> la clase puede declarar beans.</li>
 * <li><b>Autoconfiguración:</b> Spring Boot configura JPA, caché, MVC y actuator
 * según las dependencias presentes en el classpath.</li>
 * <li><b>Escaneo de componentes:</b> busca controladores, servicios y repositorios
 * en el paquete {@code cl.utem.inf.cpyd} y sus subpaquetes.</li>
 * </ul>
 */
@SpringBootApplication
public class CpydApplication {

    /**
     * Arranca el contexto de Spring y el servidor web embebido. El puerto por defecto
     * es 8080 y puede cambiarse con la propiedad {@code server.port}.
     *
     * @param args argumentos de línea de comandos; Spring los interpreta como propiedades.
     */
    public static void main(String[] args) {
        SpringApplication.run(CpydApplication.class, args);
    }
}
