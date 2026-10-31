# [US-11] Microservicio de Órdenes de Recolocación (`relocations`)

- **Puerto de desarrollo**: 8110
- **Base de datos**: `relocations_db` (PostgreSQL)
- **Responsabilidad**: Directrices de redistribución de recursos entre zonas con exceso y déficit a partir de demanda. Asignación de vehículos de transporte y control de traslados.
- **Eventos principales**: `RelocationOrderCreated`, `RelocationOrderCompleted`.
