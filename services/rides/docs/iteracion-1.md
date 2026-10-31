# Cierre de la primera iteración de Viajes

Fecha: 2026-10-31. Estado: **cerrada para demostración local**. Implementación de referencia: commit `a1d8bb8` en la rama `feature/us-08-rides-microservice`.

## Alcance entregado

- API para iniciar (`POST /rides`), consultar (`GET /rides/{id}`) y finalizar (`POST /rides/{id}/finish`) viajes.
- Estados `IN_PROGRESS` y `FINISHED`, exclusión de viajes activos por usuario y vehículo, validación de reservas y zonas mediante el adaptador demo.
- Duración en segundos enteros y coste preliminar EUR con tarifa conservada al inicio y mínimo de un minuto; sin cobros.
- Idempotencia persistente de inicio y finalización, restricciones de base de datos y bloqueo de finalización.
- Persistencia JPA/JDBC, migraciones Flyway, H2 local y PostgreSQL en Compose.
- Eventos `RideStarted` y `RideFinished` mediante outbox transaccional; publicación RabbitMQ opcional con confirmaciones y reintentos.
- Contrato OpenAPI, esquema de eventos, página de bienvenida, salud, métricas y logs con correlación.
- Maven Wrapper, Dockerfile, Compose y workflow de CI con pruebas de integración.

La [guía del servicio](../README.md) contiene comandos y reglas. La [arquitectura de Viajes](arquitectura.md) y los [ADR](../../../docs/adr/README.md) explican límites y decisiones.

## Evidencias de verificación

| Comprobación | Resultado observado en esta iteración |
| --- | --- |
| `mvn verify` durante la construcción Docker con Java 21 | Construcción correcta; 13 pruebas de integración superadas, 0 fallos y 1 prueba de RabbitMQ real omitida por su condición de entorno |
| Estilo durante esa construcción | 0 infracciones de Checkstyle |
| HTTP después de reconstruir el contenedor | `/`, `/openapi.yaml` y `/actuator/health` devolvieron 200; contenedor saludable |
| Ciclo manual en PowerShell | Inicio, consulta y finalización realizados; viaje `73e615a5-d08d-4e11-879d-46b6df933282` finalizado, 90 segundos y 0,70 EUR |
| Repetición manual de finalización con los mismos parámetros | Viaje `4fef90f0-09c5-4c84-8dac-74c683afc229`: ambas respuestas `FINISHED`, mismo `finishedAt` (`2026-10-31T13:14:42.347773Z`), duración 0 segundos y coste 0,60 EUR |

Los UUID anteriores son evidencias de la sesión local, no datos que deban existir en una instalación nueva. La prueba manual repetida acredita la estabilidad de la respuesta; no se inspeccionó su outbox manualmente. Las pruebas automáticas sí verifican que el reintento conserva el número de eventos.

Esta evidencia no acredita la ejecución del workflow remoto, de las pruebas contra PostgreSQL ni de la prueba condicional de transporte RabbitMQ real. Compose sí se ejecutó con PostgreSQL y RabbitMQ saludables, pero eso no sustituye esas pruebas. Tampoco se ejecutó en esta sesión el escenario manual de caída y recuperación del broker.

## Incidencias locales resueltas o explicadas

- La raíz `/` devolvía 404: se añadió `static/index.html` y se verificó respuesta 200.
- Maven ejecutaba clases Java 21 con Java 17: debe seleccionarse JDK 21; no era un fallo de las pruebas de negocio.
- Arranque Maven con puerto 8082 ocupado: evitar la segunda instancia en el mismo puerto o usar `PORT=8083`.
- Reintento de finalización con 409: el código exige conservar clave y destino originales. Un nuevo ensayo con los mismos parámetros devolvió dos respuestas iguales.

## Pendientes para la siguiente iteración

| Trabajo | Criterio para abordarlo |
| --- | --- |
| Sustituir `DemoMobilityGateway` | Acordar contratos con Disponibilidad, Reservas y Zonas; claim/liberación atómicos e idempotentes por `rideId` |
| Coordinar operaciones remotas | Concretar la propuesta [ADR-0006](../../../docs/adr/0006-integracion-remota-coordinada.md), recuperación persistente, timeouts y compensaciones |
| Autenticación y autorización | Resolver usuario desde identidad autenticada y comprobar propiedad del viaje en consulta/finalización |
| Consumidores reales | Colas propias, deduplicación por `eventId`, compatibilidad y responsabilidades sobre reservas y disponibilidad |
| Verificación de integración | Ejecutar pruebas dedicadas de PostgreSQL y RabbitMQ y el escenario de caída/recuperación; conservar informes |
| Operación fuera de demo | Definir secretos, acceso, despliegue, alertas, retención y capacidad de la outbox |

Historial global y facturación/cobros quedan fuera del alcance de esta entrega. El cierre no cambia los límites de seguridad ni las garantías de entrega al menos una vez de los eventos.


## Preparación de la PR hacia `develop`

La refactorización `33a1b22` se integró en `feature/us-08-rides-microservice`. Se incorpora la base de `develop` conservando su infraestructura, el código de Reservas y su documentación global. La demo se selecciona explícitamente con `compose.rides.yaml`; no reemplaza `docker-compose.yml` ni se añade al reactor Maven raíz. La adaptación al Java 17 y al puerto 8107 del entorno común queda pendiente de una iteración de integración; esta PR conserva la demo Java 21 y el puerto 8082 ya verificados.

Después de incorporar esa base se ejecutó de nuevo `mvnw.cmd -B -ntp clean verify` con Java 21: construcción correcta, 0 infracciones de estilo, 13 pruebas superadas y 1 prueba condicional de RabbitMQ omitida. También validaron `docker compose -f compose.rides.yaml config --quiet` y `docker compose -f docker-compose.yml config --quiet`. Estas comprobaciones no arrancan ni modifican los contenedores existentes.
