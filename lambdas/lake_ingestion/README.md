# Lambda: Ingesta en Data Lake S3 (`lake_ingestion`)

### Responsabilidad
- Conectar con SQS / colas de eventos en RabbitMQ.
- Ingerir los eventos de dominio (`VehicleReserved`, `ReservationCancelled`, `ReservationExpired`, etc.) en la capa `raw/` de Amazon S3 en formato inmutable JSON particionado.
