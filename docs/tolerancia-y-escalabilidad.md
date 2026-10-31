# Tolerancia a Fallos y Escalabilidad

## Mecanismos Incorporados
1. Reintentos y colas para desacoplar productores y consumidores de eventos.
2. Dead-letter queue (DLQ) para eventos fallidos.
3. Operaciones idempotentes basadas en `event_id` y claves de unicidad.
4. Health checks de microservicios y políticas de reinicio de contenedores.
5. Escalabilidad horizontal independiente de servicios de alta carga (reservas y viajes).
