# [US-04] Microservicio de Inventario de Recursos (`inventory`)

- **Puerto de desarrollo**: 8103
- **Base de datos**: `inventory_db` (PostgreSQL)
- **Responsabilidad**: Registro de bicicletas y patinetes como activos: identificador único, tipo (BIKE, SCOOTER), fecha de compra, modelo, fabricante y estado administrativo.
- **Restricción explícita**: No gestiona disponibilidad en tiempo real ni bloqueos de reservas.
