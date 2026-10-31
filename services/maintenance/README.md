# [US-10] Microservicio de Órdenes de Mantenimiento (`maintenance`)

- **Puerto de desarrollo**: 8109
- **Base de datos**: `maintenance_db` (PostgreSQL)
- **Responsabilidad**: Flujo de intervención técnica: creación de órdenes, asignación a operarios, retirada (`WITHDRAWN`), fases de taller (`CHARGING`, `REPAIRING`, `READY_FOR_SERVICE`) y retorno a la calle (`AVAILABLE`).
- **Eventos principales**: `VehicleWithdrawnForMaintenance`, `VehicleReturnedToService`.
