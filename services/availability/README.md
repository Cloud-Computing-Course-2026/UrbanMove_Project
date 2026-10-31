# [US-05] Microservicio de Disponibilidad (`availability`)

- **Puerto de desarrollo**: 8104
- **Base de datos**: `availability_db` (PostgreSQL)
- **Responsabilidad**: Máquina de estados operativa en tiempo real de los recursos y su ubicación/zona actual. Controla los 9 estados del ciclo de vida y el bloqueo exclusivo para evitar doble asignación concurrente.
