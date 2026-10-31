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
