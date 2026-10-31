# ADR-002: Aislamiento Estricto de Datos y Control de Concurrencia en Reservas

## Estado
Aceptado

## Contexto
Se requiere evitar a nivel estricto la doble reserva simultánea de un mismo vehículo y garantizar que un usuario solo mantenga una reserva activa.

## Decisión
1. Una base de datos PostgreSQL exclusiva por microservicio (15 bases aisladas). Cero joins cruzados.
2. En el microservicio de reservas, se implementa bloqueo pesimista `SELECT ... FOR UPDATE` en JPA combinado con índices parciales únicos en PostgreSQL:
   - `uq_active_reservation_resource`: único `PENDING` por recurso.
   - `uq_active_user_reservation`: única reserva `PENDING` por usuario.

## Consecuencias
- Cero condiciones de carrera sobre el mismo activo.
- Integridad física y lógica garantizada sin sobrecargar otros servicios.
