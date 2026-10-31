# ADR-0007: Organización de Viajes por responsabilidad

- Estado: Aceptado (implementado)
- Fecha de registro: 2026-10-31
- Alcance: Paquetes Java de `services/rides`

## Contexto

La primera iteración agrupaba las clases en `com.urbanmove.rides` y permitía acceso directo a campos y métodos con visibilidad de paquete. Tras cerrar la demo, se solicita distribuir el código por responsabilidad para facilitar su mantenimiento.

## Decisión

Mantener `RidesApplication` en la raíz y distribuir las clases en `controller`, `dto`, `service`, `model`, `repository`, `exception`, `integration`, `integration.demo`, `messaging`, `messaging.outbox` y `observability`. Conservar los recursos y las pruebas de integración en sus ubicaciones actuales.

Encapsular los campos de `Ride`: exponer lectura mediante getters y agrupar las escrituras de inicio y finalización en métodos de la entidad. `RideService` conserva las validaciones, el cálculo de precios y los límites transaccionales. Hacer accesibles las fábricas de errores, el estado de la excepción, la conversión a DTO y la escritura de outbox que necesitan consumidores en otros paquetes.

La reorganización mantiene los contratos HTTP y de eventos, las tablas y las garantías existentes. No introduce separación hexagonal completa: siguen existiendo dependencias de Spring/JPA en el modelo, de DTO y outbox concretos en el servicio, y de `HttpStatus` en los errores.

## Alternativas

- Conservar el paquete único: evita ajustes de visibilidad, pero no distingue responsabilidades.
- Hacer públicos los campos de la entidad: simplifica el movimiento y permite mutaciones parciales desde cualquier clase; se descarta en favor de encapsulación.
- Rediseñar puertos y adaptadores para todas las capas: ampliaría el alcance más allá de la reorganización solicitada.

## Consecuencias y verificación

Los imports y los enlaces de documentación deben usar las nuevas rutas. Mantener la aplicación en el paquete padre conserva el descubrimiento automático de componentes, entidades y repositorios. Las transiciones de estado de la entidad no sustituyen la validación y coordinación transaccional del servicio.

Ejecutar `mvnw clean verify` con Java 21 para comprobar estilo, compilación, integración HTTP/BD, idempotencia y concurrencia. La prueba RabbitMQ real continúa condicionada a `RUN_RABBIT_TESTS=true`.

Verificación local del 2026-10-31: `mvnw.cmd -B -ntp clean verify` con Java 21 terminó correctamente, sin infracciones de Checkstyle, con 13 pruebas superadas y la prueba condicional de RabbitMQ omitida. Se comprobaron también 66 enlaces relativos de documentación, sin rutas inexistentes. Esta ejecución usó H2; no acredita pruebas con PostgreSQL o RabbitMQ real.

Referencias: [estructura del servicio](../../services/rides/README.md#estructura-del-código), [entidad](../../services/rides/src/main/java/com/urbanmove/rides/model/Ride.java) y [servicio](../../services/rides/src/main/java/com/urbanmove/rides/service/RideService.java).
