# Arquitectura del Sistema URBANMOVE

## 1. Vista de Contexto
- **Usuarios finales**: Interactúan con la app móvil (8080) para reservar y viajar.
- **Operarios y técnicos**: Interactúan con la app de mantenimiento (8081).
- **Directivos / Operaciones**: Dashboard centralizado (8082).

## 2. Vista de Contenedores
- **Backend Transaccional**: 15 microservicios atómicos desarrollados en Java 17 + Spring Boot 3 en puertos 8100 a 8114.
- **Bases de Datos Aisladas**: 15 esquemas independientes en PostgreSQL 16. Cero joins entre esquemas.
- **Bus de Mensajería**: RabbitMQ / AWS SQS para desacoplar transacciones mediante eventos de dominio.
- **Data Lake (S3)**: Capas `raw/`, `trusted/` y `curated/` consumidas por Data Marts.
- **Funciones Serverless**: Lambdas de expiración, mantenimiento, ingesta, marts y recolocación.


## Demo de Viajes [US-08]

Viajes incorpora una primera iteración autónoma con Java 21, H2/PostgreSQL y outbox RabbitMQ. Su ejecución está separada del entorno común mediante `compose.rides.yaml`; no sustituye la infraestructura ni integra todavía el servicio de Reservas. El puerto de la demo es 8082, mientras que la asignación prevista para la integración de Viajes es 8107.

La [arquitectura detallada de Viajes](../services/rides/docs/arquitectura.md), su [guía de ejecución](../services/rides/README.md) y el [registro ADR](adr/README.md) documentan las garantías locales y los acuerdos de integración pendientes. El POM raíz conserva Java 17 y sus módulos actuales; Viajes se compila con su propio Maven Wrapper y JDK 21.
