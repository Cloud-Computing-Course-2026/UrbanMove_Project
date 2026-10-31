# Lambda: Expiración Automática de Reservas (`reservation_expiry`)

Función serverless programada (AWS EventBridge cron cada 1 minuto) o invocada bajo demanda.

### Responsabilidad
- Conectarse al microservicio de reservas a través de su endpoint protegido interno:
  `POST /api/v1/internal/reservations/expire-timed-out`
- Proporcionar la cabecera `X-Internal-Token` para autenticación interna entre servicios.
- Provocar la transición de reservas caducadas a `EXPIRED`, liberando los vehículos a `AVAILABLE` y publicando el evento `ReservationExpired`.
