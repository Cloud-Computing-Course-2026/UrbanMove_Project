# [US-09] Microservicio de Incidencias (`incidents`)

- **Puerto de desarrollo**: 8108
- **Base de datos**: `incidents_db` (PostgreSQL)
- **Responsabilidad**: Registro, clasificación y gestión del ciclo de vida de incidencias sobre la flota (averías mecánicas, vandalismo, desaparición, bloqueo).
- **Regla crítica**: Si se marca como crítica (ej. fallo de frenos), solicita la retirada inmediata del recurso.
