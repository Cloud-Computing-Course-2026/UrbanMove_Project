# Lambda: Detección de Mantenimiento (`maintenance_detection`)

**Función Serverless Obligatoria** según el enunciado oficial:
- **Disparador**: Consumo de eventos `VehicleBatteryLow`, incidencias críticas o umbral de usos acumulados.
- **Responsabilidad**: Generar de forma idempotente una orden de mantenimiento hacia `maintenance` para la retirada y recarga/reparación del vehículo.
