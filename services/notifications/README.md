# [US-14] Microservicio de Notificaciones (`notifications`)

- **Puerto de desarrollo**: 8113
- **Base de datos**: `notifications_db` (PostgreSQL)
- **Responsabilidad**: Envío o simulación de alertas y avisos (push, email, SMS) a usuarios y operarios ante reservas, cancelaciones, incidencias graves o batería baja. Idempotente para no duplicar mensajes.
