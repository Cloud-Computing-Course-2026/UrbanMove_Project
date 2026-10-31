# ADR-0004: Outbox transaccional y entrega al menos una vez

- Estado: Aceptado (implementado; registro retrospectivo)
- Fecha de registro: 2026-10-31
- Fecha de decisión original: No consta
- Alcance: Publicación de RideStarted y RideFinished

## Contexto

El cambio del viaje debe conservar su evento aunque RabbitMQ esté caído. Un commit de BD y un envío al broker son operaciones independientes.

## Decisión

Guardar viaje y evento en la misma transacción local. Un publicador consulta la outbox por orden de secuencia entre los eventos elegibles, bloquea una fila y envía mediante `EventTransport`. RabbitMQ usa mensajes persistentes, exchange durable `urbanmove.events`, confirmaciones y envío obligatorio con comprobación de devoluciones. Marcar publicado solo si el transporte confirma el envío. Persistir intentos y reintentar con espera exponencial limitada a 60 segundos.

Publicar snapshots con `eventId`, `schemaVersion: 1` y `correlationId`. Las routing keys son `RideStarted` y `RideFinished`. La cola durable `urbanmove.rides.events` sirve para inspección; cada consumidor necesita su propia cola y bindings.

## Alternativas

Contraste retrospectivo: enviar después del commit sin outbox permite perder eventos; enviar antes puede publicar cambios que luego se revierten. Una transacción distribuida o captura de cambios de BD añadiría infraestructura y operación ausentes en esta iteración.

## Consecuencias

La entrega es al menos una vez: una caída entre envío y marcado puede duplicar mensajes. Los consumidores deben deduplicar por `eventId` y evitar regresiones de estado; no se garantiza orden global. La confirmación del broker no prueba que un consumidor haya procesado el evento.

Se mantiene la transacción de BD durante el envío y se procesa como máximo un evento por tick. No hay descarte, DLQ automática ni retención de publicados. Aumentar carga exige revisar throughput, bloqueo, almacenamiento y recuperación de eventos que fallan persistentemente.

## Verificación y seguimiento

Evidencia: [Outbox](../../services/rides/src/main/java/com/urbanmove/rides/Outbox.java), [publicador](../../services/rides/src/main/java/com/urbanmove/rides/OutboxPublisher.java), [transporte](../../services/rides/src/main/java/com/urbanmove/rides/RabbitEvents.java) y [contrato](../../services/rides/docs/events.schema.json). Existen pruebas de recuperación con transporte simulado y una [prueba condicional con RabbitMQ](../../services/rides/src/test/java/com/urbanmove/rides/RabbitTransportTest.java). Revisar al incorporar consumidores reales o aumentar la carga.
