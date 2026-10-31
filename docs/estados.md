# Nueve Estados del Recurso (Ciclo de Vida)

| Estado | Significado | Transición disparada por |
| :--- | :--- | :--- |
| `AVAILABLE` | Se puede reservar o usar | Devolución de viaje, cancelación/expiración o fin de mantenimiento |
| `RESERVED` | Bloqueado temporalmente por reserva | `VehicleReserved` |
| `IN_USE` | Viaje activo en curso | `RideStarted` |
| `LOW_BATTERY` | Batería baja (<20%) pendiente de retirada | `VehicleBatteryLow` |
| `WITHDRAWN` | Retirado temporalmente de la vía pública | `VehicleWithdrawnForMaintenance` |
| `CHARGING` | En proceso de recarga eléctrica | Orden de recarga en taller |
| `REPAIRING` | En reparación de avería mecánica | Orden de reparación |
| `READY_FOR_SERVICE` | Revisión técnica superada, listo para calle | Fin de intervención en taller |
| `RETIRED` | Baja administrativa definitiva | Invalidez técnica / fin de vida útil |
