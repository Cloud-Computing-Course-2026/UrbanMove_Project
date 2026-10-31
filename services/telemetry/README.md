# [US-06] Microservicio de Batería y Telemetría (`telemetry`)

- **Puerto de desarrollo**: 8105
- **Base de datos**: `telemetry_db` (PostgreSQL)
- **Responsabilidad**: Ingesta y registro de telemetría periódica de los vehículos: porcentaje de batería, coordenadas GPS, kilómetros totales y usos desde la última revisión.
- **Evento principal**: Emisión de `VehicleBatteryLow` cuando la batería cae por debajo del 20%.
