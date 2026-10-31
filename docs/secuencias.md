# Diagramas de Secuencia Principales

Contiene las 4 secuencias requeridas:
1. **Flujo de Reserva**: Usuario solicita -> bloqueo pesimista en availability/reservas -> emisión `VehicleReserved`.
2. **Flujo de Inicio de Viaje**: Usuario confirma reserva -> vehículo pasa a `IN_USE` -> inicio de ride -> `RideStarted`.
3. **Flujo de Retirada por Mantenimiento**: Batería baja / avería -> Lambda detecta -> orden de taller -> vehículo a `WITHDRAWN`.
4. **Flujo de Recolocación**: Mart de demanda detecta déficit -> propuesta de orden -> asignación de operario -> transporte entre zonas -> `RelocationOrderCompleted`.
