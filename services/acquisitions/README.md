# [US-12] Microservicio de Adquisiciones (`acquisitions`)

- **Puerto de desarrollo**: 8111
- **Base de datos**: `acquisitions_db` (PostgreSQL)
- **Responsabilidad**: Registro de compras de lotes de vehículos a proveedores, costes de adquisición y emisión del evento `VehiclePurchased` para que `inventory` y analítica reconozcan los nuevos activos.
