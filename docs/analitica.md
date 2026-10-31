# Data Lake y Data Marts (Capa Analítica)

## 1. Capas del Data Lake (Amazon S3 / MinIO)
- `raw/`: Evento original inmutable tal cual llega desde el bus asíncrono.
- `trusted/`: Eventos normalizados, validados y limpios de duplicados.
- `curated/`: Tablas de hechos y dimensiones agregadas listas para alimentar los Data Marts.

## 2. Los Siete Data Marts
1. **Usuarios**: Usuarios activos, viajes e histórico de cancelaciones.
2. **Reservas**: Caducidades y reservas no consumidas por zona y tipo.
3. **Viajes**: Orígenes y destinos más frecuentes, duración media y facturación.
4. **Flota**: Recursos por zona, tipo y estado operativo.
5. **Mantenimiento**: Tiempos fuera de servicio y tasa de averías.
6. **Operaciones**: Órdenes de recolocación creadas, completadas y retrasos.
7. **Demanda**: Balance de déficit/exceso por zona y franja horaria.
