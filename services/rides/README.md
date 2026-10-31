# [US-08] Microservicio de Viajes (`rides`)

- **Puerto de desarrollo**: 8107
- **Base de datos**: `rides_db` (PostgreSQL)
- **Responsabilidad**: Gestión de inicio y fin de trayectos en tiempo real. Validación de zonas, cálculo de duración, distancia y coste económico estimado.
- **Eventos principales**: `RideStarted`, `RideFinished`.
