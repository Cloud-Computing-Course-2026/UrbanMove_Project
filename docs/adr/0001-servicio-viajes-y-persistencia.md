# ADR-0001: Servicio Viajes con persistencia relacional propia

- Estado: Aceptado (implementado; registro retrospectivo)
- Fecha de registro: 2026-10-31
- Fecha de decisión original: No consta
- Alcance: `services/rides`, persistencia y entorno local

## Contexto

La primera iteración necesita gestionar el ciclo de viaje con transacciones y restricciones de unicidad, ejecutarse localmente y ofrecer un contrato de integración. Solo Viajes está implementado en este repositorio.

## Decisión

Mantener Viajes como aplicación Java 21 / Spring Boot con Maven Wrapper, API HTTP y almacenamiento relacional propio. Usar JPA para viajes y JDBC para outbox/demo. Flyway aplica migraciones y Hibernate valida el esquema. Usar H2 persistente para ejecución local sencilla y PostgreSQL en Compose. El historial global queda fuera del servicio.

## Alternativas

Alternativas de contraste retrospectivo, sin debate previo acreditado: una base compartida acoplaría los futuros servicios; persistencia solo en memoria perdería estado al reiniciar; usar únicamente PostgreSQL aumentaría los requisitos del arranque local, aunque simplificaría la paridad entre entornos.

## Consecuencias

Las transacciones y restricciones viven en la BD de Viajes. Mantener H2 y PostgreSQL exige comprobar diferencias de SQL y bloqueo. H2 no sustituye la verificación con PostgreSQL. El entorno Compose no define una arquitectura de producción.

## Verificación y seguimiento

Evidencia: [pom.xml](../../services/rides/pom.xml), [configuración](../../services/rides/src/main/resources/application.yml), [migración](../../services/rides/src/main/resources/db/migration/V1__rides_and_outbox.sql) y [Compose](../../compose.yaml).

El [workflow](../../.github/workflows/rides.yml) define pruebas en ambas bases. Registro basado en inspección; no acredita ejecución de CI. Revisar al introducir otro servicio o cambiar el motor de datos.
