# [US-15] Microservicio de Analítica Operacional (`analytics`)

- **Puerto de desarrollo**: 8114
- **Base de datos**: `analytics_db` / Vistas de Data Marts
- **Responsabilidad**: Consultas agregadas de demanda, ocupación por zonas, estado de flota y KPIs para el Dashboard Operativo sin realizar bloqueos sobre las tablas operacionales.
- **Evento principal**: Emisión de `DemandHotspotDetected` ante desbalances de oferta/demanda.
