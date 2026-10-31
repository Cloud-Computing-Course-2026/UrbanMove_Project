# Matriz de Trazabilidad de Requisitos (48 Requisitos)

| # | Apartado | Requisito | Ruta en Repositorio | Evidencia de Cierre |
| :--- | :--- | :--- | :--- | :--- |
| 1 | 3.1 | App Usuarios: consulta, reserva y viajes | `clients/users/` | Interfaz y endpoints REST |
| 2 | 3.2 | App Mantenimiento: órdenes y taller | `clients/maintenance/` | Interfaz y endpoints REST |
| 3 | 3.3 | Dashboard Operativo: KPIs globales | `clients/dashboard/` | Métricas reales agregadas |
| 4 | 4.1 | Microservicio de Identidad (8100) | `services/identity/` | Auth JWT y roles |
| 5 | 4.1 | Microservicio de Perfiles (8101) | `services/profiles/` | Contactos y baja lógica |
| 6 | 4.1 | Microservicio de Zonas (8102) | `services/zones/` | Catálogo y bounding box |
| 7 | 4.1 | Microservicio de Inventario (8103) | `services/inventory/` | Registro de activos flota |
| 8 | 4.1 | Microservicio de Disponibilidad (8104) | `services/availability/` | Máquina de estados y lock |
| 9 | 4.1 | Microservicio de Telemetría (8105) | `services/telemetry/` | Batería y alertas |
| 10 | 4.1 | Microservicio de Reservas (8106) | `services/reservations/` | Concurrencia y TTL |
| 11 | 4.1 | Microservicio de Viajes (8107) | `services/rides/` | Trayectos y coste |
| 12 | 4.1 | Microservicio de Incidencias (8108) | `services/incidents/` | Reportes y criticidad |
| 13 | 4.1 | Microservicio de Mantenimiento (8109) | `services/maintenance/` | Órdenes de taller |
| 14 | 4.1 | Microservicio de Recolocación (8110) | `services/relocations/` | Traslados entre zonas |
| 15 | 4.1 | Microservicio de Adquisiciones (8111) | `services/acquisitions/` | Lotes de compra |
| 16 | 4.1 | Microservicio de Historial (8112) | `services/history/` | Proyección desacoplada |
| 17 | 4.1 | Microservicio de Notificaciones (8113)| `services/notifications/`| Alertas e idempotencia |
| 18 | 4.1 | Microservicio de Analítica (8114) | `services/analytics/` | Consultas de marts |
| 19 | 4.3 | Lambda Expiración Reservas | `lambdas/reservation_expiry/`| Liberación por timeout |
| 20 | 4.3 | Lambda Detección Mantenimiento | `lambdas/maintenance_detection/`| Órdenes por batería/usos |
| 21 | 4.3 | Lambda Ingesta Data Lake | `lambdas/lake_ingestion/` | S3 raw particionado |
| 22 | 5.0 | 9 Estados del Recurso | `docs/estados.md` | Ciclo de vida explícito |
| 23 | 6.0 | 13 Eventos con correlation_id | `docs/eventos.md` | JSON Schema y AsyncAPI |
| 24 | 8.1 | Data Lake S3: raw, trusted, curated | `infra/` + MinIO | Almacenamiento desacoplado |
| 25 | 8.2 | 4+ Data Marts Analíticos | `docs/analitica.md` | Esquemas y consultas |
| ... | ... | ... | ... | ... |
