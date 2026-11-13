# Glosario

Términos técnicos usados en el proyecto, en orden alfabético.

| Término | Definición |
|---|---|
| **API (Application Programming Interface)** | Conjunto de operaciones que un programa ofrece para que otros programas lo usen. |
| **API key** | Secreto de largo plazo que identifica a un usuario ante la API. Aquí se cambia por un JWT. |
| **Autenticación** | Comprobar *quién* eres (login). |
| **Autorización** | Comprobar *qué* puedes hacer. |
| **Bearer** | Esquema de la cabecera `Authorization`: quien "porta" (*bear*) el token, tiene acceso. Se escribe `Authorization: Bearer <token>`. |
| **Caché** | Copia temporal de datos en memoria para no volver a pedirlos a la fuente original. |
| **Caffeine** | Biblioteca Java de caché en memoria, rápida y segura entre hilos. |
| **Cache stampede** | Muchos hilos piden a la vez un dato que acaba de expirar, saturando la base de datos. |
| **Claim** | Dato dentro de un JWT (por ejemplo, `sub`, `exp`). |
| **Concurrencia** | Varias tareas progresan de forma intercalada, aunque no necesariamente al mismo instante. |
| **Context path** | Prefijo común de todas las rutas de una aplicación web. Aquí: `/cpyd`. |
| **Controlador (controller)** | Clase que recibe peticiones HTTP y devuelve respuestas. |
| **CSV** | Archivo de texto con valores separados por comas; una fila por registro. |
| **DTO (Data Transfer Object)** | Objeto sencillo que solo transporta datos entre capas o por la red. |
| **Entidad (entity)** | Clase Java asociada a una tabla de la base de datos. |
| **Enum** | Tipo con un conjunto fijo de valores (`CHILE`, `COLOMBIA`, `ECUADOR`). |
| **Expiración** | Momento a partir del cual un dato o token deja de ser válido. |
| **H2** | Base de datos en memoria, escrita en Java, usada para pruebas. |
| **Hash** | Resultado irreversible de aplicar una función a un texto; sirve para guardar claves sin guardarlas en claro. |
| **HMAC / HS256** | Firma criptográfica que usa una clave compartida. HS256 = HMAC con SHA-256. |
| **HTTP** | Protocolo de la web. Define métodos (`GET`, `POST`), códigos de estado y cabeceras. |
| **Hibernate** | Implementación de JPA que traduce objetos Java a filas SQL. |
| **Idempotente** | Una operación que da el mismo resultado aunque se repita. `GET` lo es. |
| **Inyección de dependencias** | Técnica en que el framework entrega a una clase los objetos que necesita, en vez de que ella los cree. |
| **JPA** | Especificación de Java para mapear objetos a bases de datos relacionales. |
| **JSON** | Formato de texto para intercambiar datos estructurados. |
| **JWT (JSON Web Token)** | Token firmado con forma `cabecera.contenido.firma` que acredita una identidad por un tiempo limitado. |
| **Log injection** | Ataque que inserta saltos de línea en un dato para falsificar líneas del registro. |
| **Maven** | Herramienta que compila, prueba y empaqueta proyectos Java. `mvnw` es su *wrapper*, que se descarga solo. |
| **Mock** | Objeto falso que reemplaza a una dependencia real en una prueba. |
| **MockMvc** | Herramienta de Spring para probar controladores sin levantar un servidor. |
| **Paralelismo** | Varias tareas se ejecutan **realmente** al mismo tiempo, en distintos núcleos. |
| **PostgreSQL** | Sistema de gestión de bases de datos relacional de código abierto. |
| **Proxy (Spring)** | Objeto intermediario que añade comportamiento (caché, transacciones) a otro. |
| **ProblemDetail / RFC 7807** | Formato estándar para describir errores en respuestas HTTP. |
| **Rate limiting** | Restringir cuántas peticiones puede hacer un cliente en un periodo. |
| **Repositorio (repository)** | Interfaz que encapsula el acceso a la base de datos. |
| **REST** | Estilo de API sobre HTTP donde las URL representan recursos. |
| **Spring Boot** | Framework Java que configura automáticamente una aplicación lista para ejecutar. |
| **Swagger UI / OpenAPI** | Estándar para describir APIs y su página web interactiva. |
| **Timing attack** | Deducir un secreto midiendo cuánto tarda una comparación. |
| **Tipo de cambio** | Cuántas unidades de una moneda equivalen a una de otra. Aquí: moneda local por 1 USD. |
| **UUID** | Identificador de 128 bits prácticamente único. La versión 7 incluye la marca de tiempo. |
| **WAR** | Archivo empaquetado de una aplicación web Java para desplegar en un servidor como Tomcat. |
| **yfinance** | Biblioteca Python que descarga datos de Yahoo Finance. |
