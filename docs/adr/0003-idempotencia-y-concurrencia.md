# ADR-0003: Idempotencia y concurrencia en base de datos

- Estado: Aceptado (implementado; registro retrospectivo)
- Fecha de registro: 2026-10-31
- Fecha de decisión original: No consta
- Alcance: Inicio y finalización de viajes

## Contexto

Los clientes pueden repetir peticiones y dos solicitudes pueden intentar usar el mismo vehículo o usuario. Es necesario impedir viajes y eventos duplicados sin depender de un único proceso Java.

## Decisión

Exigir `Idempotency-Key` en ambos POST y conservar `start_key` y `finish_key` con unicidad independiente por operación, global dentro de la BD del servicio. Comparar los datos de un reintento y devolver conflicto si no corresponden. Al repetir un inicio válido se devuelve el estado actual del viaje, con `201` y la misma `Location`; no se almacena una copia de la respuesta original.

Usar columnas únicas `active_vehicle_id` y `active_user_id` mientras el viaje está activo y ponerlas a NULL al finalizar. Bloquear la fila del viaje para finalizar; el adaptador demo bloquea el vehículo al reclamarlo. La transacción incluye el cambio de viaje y su evento.

## Alternativas

Contraste retrospectivo: bloqueos o cachés en memoria no coordinan varias instancias ni sobreviven a reinicios. Un GET antes de escribir, sin restricciones de BD, permite carreras. Almacenar respuestas completas proporcionaría otra semántica de repetición, con más almacenamiento y mantenimiento.

## Consecuencias

La BD arbitra la exclusión. Los conflictos concurrentes pueden producir `409` y requerir reintento con la misma clave. Las claves se conservan con los viajes, sin política de caducidad independiente. Estas restricciones no garantizan disponibilidad en un servicio remoto.

## Verificación y seguimiento

Evidencia: [RideService](../../services/rides/src/main/java/com/urbanmove/rides/service/RideService.java), [bloqueo de finalización](../../services/rides/src/main/java/com/urbanmove/rides/repository/RideRepository.java) y [restricciones SQL](../../services/rides/src/main/resources/db/migration/V1__rides_and_outbox.sql). Las [pruebas existentes](../../services/rides/src/test/java/com/urbanmove/rides/RidesIntegrationTest.java) incluyen reintentos e inicios/finalizaciones concurrentes. Revisar al añadir identidad autenticada, retención o particionado de datos.
