# Tabla de Propiedad de Datos (Data Ownership)

Cada tabla o entidad pertenece exclusivamente a un microservicio. No existen escrituras cruzadas ni joins directos entre bases de datos:

| Microservicio | Base de Datos | Entidades y Tablas Principales |
| :--- | :--- | :--- |
| `identity` | `identity_db` | `users_auth`, `roles`, `tokens` |
| `profiles` | `profiles_db` | `user_profiles`, `contact_methods` |
| `zones` | `zones_db` | `zones`, `geofence_polygons`, `parking_rules` |
| `inventory` | `inventory_db` | `assets`, `models`, `manufacturers` |
| `availability` | `availability_db` | `resource_state`, `locks` |
| `telemetry` | `telemetry_db` | `telemetry_snapshots`, `metrics_log` |
| `reservations` | `reservations_db` | `reservations`, `vehicles` (proyección local de lock) |
| `rides` | `rides_db` | `rides`, `ride_legs`, `fares` |
| `incidents` | `incidents_db` | `incidents`, `reports` |
| `maintenance` | `maintenance_db` | `maintenance_orders`, `task_logs` |
| `relocations` | `relocations_db` | `relocation_orders`, `fleet_assignments` |
| `acquisitions` | `acquisitions_db` | `purchase_batches`, `supplier_records` |
| `history` | `history_db` | `user_history_events`, `vehicle_life_events` |
| `notifications`| `notifications_db`| `notification_logs`, `outbox` |
| `analytics` | `analytics_db` | Vistas materializadas de Data Marts |
