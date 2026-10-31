# Funciones Serverless (Lambdas)

1. `reservation_expiry`: Vence reservas en estado `PENDING` cuyo plazo (`expires_at`) haya pasado.
2. `maintenance_detection`: Analiza `VehicleBatteryLow`, averías o umbral de usos para emitir órdenes automáticas de mantenimiento.
3. `lake_ingestion`: Ingesta eventos del bus hacia S3 en particiones `raw/`.
4. `data_marts`: Normaliza hacia `trusted/` y recalcula agregados para `curated/`.
5. `relocation_proposal`: Analiza balance de oferta/demanda y propone órdenes de recolocación.
