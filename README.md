# utem-cpyd-api

Proyecto auxiliar para el **primer taller de Computación Paralela y Distribuida (CPyD)** de la Universidad Tecnológica Metropolitana (UTEM), semestre de primavera.

Es una API REST pequeña que entrega el **tipo de cambio diario del dólar (USD)** para tres países: **Chile**, **Colombia** y **Ecuador**. Los datos provienen de Yahoo Finance y se guardan en PostgreSQL.

> **¿Para qué sirve en el curso?** Es la *fuente de datos remota* del taller. Tu trabajo será consumirla desde un programa propio, y la API está pensada para que ese consumo exija decisiones reales de diseño: autenticación con token, un tope de rango de fechas, un límite de intentos de login y tokens que expiran. Todo eso afecta cómo conviene repartir las consultas entre hilos o procesos.

---

## Objetivos de aprendizaje

Al trabajar con este proyecto podrás:

1. Consumir una API REST autenticada con **JWT** (obtener, enviar y renovar el token).
2. Interpretar respuestas HTTP y los errores estándar **RFC 7807** (`ProblemDetail`).
3. Observar cómo se organiza una aplicación **por capas** (controlador → servicio → repositorio → entidad).
4. Entender para qué sirve una **caché en memoria** y qué problemas introduce.
5. Diseñar un cliente **concurrente** que respete límites del servidor.

---

## Mapa de la documentación

| Documento | Contenido | Úsalo cuando… |
|---|---|---|
| [`docs/GUIA_ESTUDIANTE.md`](docs/GUIA_ESTUDIANTE.md) | Paso a paso para levantar el entorno, ejemplos de consumo y ejercicios | Es tu **punto de partida** |
| [`docs/API.md`](docs/API.md) | Referencia completa de endpoints, parámetros, respuestas y errores | Necesitas el contrato exacto |
| [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md) | Modelo de datos, creación de la BD, carga de datos y credenciales | Preparas la base de datos |
| [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) | Capas, paquetes, flujo de una petición, caché y errores | Quieres entender el código |
| [`docs/SEGURIDAD.md`](docs/SEGURIDAD.md) | JWT, API key, límite de intentos y limitaciones conocidas | Te interesa cómo se protege la API |
| [`docs/PRUEBAS.md`](docs/PRUEBAS.md) | Estrategia de pruebas y cómo ejecutarlas | Vas a modificar o probar el código |
| [`docs/GLOSARIO.md`](docs/GLOSARIO.md) | Definición de los términos técnicos usados | Encuentras una palabra desconocida |

---

## Estructura del repositorio

```
utem-cpyd-api/
├── app/                     Aplicación Spring Boot (Java 25, Maven)
│   ├── pom.xml
│   └── src/
│       ├── main/java/cl/utem/inf/cpyd/   Código fuente
│       ├── main/resources/               application.properties
│       └── test/                         Pruebas automatizadas
├── db/
│   └── 01-model.sql         Esquema de PostgreSQL (tablas e índices)
├── tools/
│   └── conversion.py        Descarga tipos de cambio y genera un CSV
├── docs/                    Documentación académica (este índice)
├── CLAUDE.md                Guía técnica breve para asistentes de código
└── README.md
```

---

## Inicio rápido

Requisitos: **Java 25**, **PostgreSQL** y (para generar datos) **Python 3** con `yfinance` y `pandas`.

```bash
# 1. Crear la base de datos y el esquema (detalles en docs/BASE_DE_DATOS.md)
createdb exchangesdb
psql exchangesdb -f db/01-model.sql

# 2. Definir la clave de firma del JWT (mínimo 32 caracteres)
export CPYD_SIGN="una-clave-secreta-de-al-menos-32-caracteres"

# 3. Levantar la API
cd app
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080/cpyd`. Los pasos completos —usuario de BD, carga de datos y creación de una credencial— están en la [guía del estudiante](docs/GUIA_ESTUDIANTE.md).

Comprobar que está viva:

```bash
curl http://localhost:8080/cpyd/actuator/health
```

Documentación interactiva (Swagger UI), generada automáticamente por springdoc:
`http://localhost:8080/cpyd/swagger-ui/index.html`

---

## Resumen de la API

| Método | Ruta | Requiere token | Descripción |
|---|---|:---:|---|
| `POST` | `/cpyd/v1/login/authenticate` | No | Cambia correo + API key por un JWT |
| `GET` | `/cpyd/v1/exchanges/{country}/{exchange_date}` | Sí | Tipo de cambio de **un día** |
| `GET` | `/cpyd/v1/exchanges/{country}?start_date=…&end_date=…` | Sí | Tipo de cambio de un **rango** de fechas |

`{country}` admite `chile`, `colombia` o `ecuador` (sin distinguir mayúsculas). Las fechas usan el formato ISO `yyyy-MM-dd`. Detalle completo en [`docs/API.md`](docs/API.md).

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 25 + Spring Boot 4.1 | Aplicación web y REST |
| Spring Data JPA / Hibernate | Acceso a la base de datos |
| PostgreSQL | Almacenamiento (H2 en memoria solo para pruebas) |
| Nimbus JOSE + JWT | Emisión y verificación de tokens |
| Caffeine + Spring Cache | Caché en memoria |
| springdoc-openapi | Swagger UI / OpenAPI |
| Maven (wrapper `mvnw`) | Construcción y pruebas |
| Python + yfinance + pandas | Generación de los datos de origen |

---

## Licencia y autoría

Ver [`LICENSE`](LICENSE). Autor: Sebastián Salazar Molina, Universidad Tecnológica Metropolitana (UTEM).
