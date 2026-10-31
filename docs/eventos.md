# Catálogo de los 13 Eventos del Sistema

Estructura común obligatoria para todos los eventos:
```json
{
  "event_id": "UUID",
  "timestamp": "ISO-8601",
  "source": "nombre-del-microservicio",
  "event_type": "NombreDelEvento",
  "schema_version": "1.0",
  "correlation_id": "UUID",
  "data": { ... }
}
```

### Lista de Eventos Oficiales:
1. `UserRegistered`: Alta de usuario y creación de perfil.
2. `VehiclePurchased`: Incorporación de nuevo vehículo (acquisitions).
3. `VehicleReserved`: Creación de reserva sobre recurso disponible (reservations).
4. `ReservationCancelled`: Cancelación voluntaria por el usuario (reservations).
5. `ReservationExpired`: Reserva liberada automáticamente por timeout (reservations).
6. `RideStarted`: Inicio de viaje (rides).
7. `RideFinished`: Fin de trayecto y cálculo de coste (rides).
8. `VehicleBatteryLow`: Alerta de batería baja (<20%) (telemetry).
9. `VehicleWithdrawnForMaintenance`: Recurso retirado a taller (maintenance).
10. `VehicleReturnedToService`: Recurso devuelto a la calle en estado AVAILABLE (maintenance).
11. `DemandHotspotDetected`: Alerta de sobredemanda en una zona (analytics).
12. `RelocationOrderCreated`: Orden de traslado creada (relocations).
13. `RelocationOrderCompleted`: Traslado de vehículos finalizado (relocations).
