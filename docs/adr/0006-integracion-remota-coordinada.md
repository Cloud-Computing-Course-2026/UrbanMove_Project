# ADR-0006: Coordinación persistente para integrar servicios remotos

- Estado: Propuesto (no implementado)
- Fecha de registro: 2026-10-31
- Fecha de aceptación: Pendiente
- Alcance: Integración futura con Disponibilidad, Reservas y Zonas
- Relacionados: [ADR-0002](0002-puerto-movilidad-y-demo.md), [ADR-0004](0004-outbox-y-eventos.md)

## Contexto

El adaptador demo participa en la transacción local. Una llamada HTTP a otro servicio no puede revertirse mediante el rollback de la BD de Viajes. Si el claim remoto funciona y el commit local falla, el vehículo puede quedar ocupado sin viaje persistido; también puede perderse la respuesta de una operación que sí se ejecutó.

## Decisión propuesta

Diseñar una coordinación persistente (saga) con estados intermedios, reintentos, timeouts y compensación o reconciliación. Identificar las operaciones remotas por `rideId` para que claim y liberación sean idempotentes. El claim debe validar disponibilidad/reserva y cambiar el estado como operación atómica en el servicio propietario.

Esta propuesta recoge la dirección indicada en el README de Viajes. Aún deben acordarse con el equipo la orquestación o coreografía, contratos concretos, estados, propiedad del consumo de reservas, tratamiento de fallos y responsable de actualizar disponibilidad al recibir eventos. No se modifican aquí el modelo ni el adaptador actual.

## Alternativas

- GET seguido de PUT remoto: no garantiza claim atómico frente a competidores.
- Llamadas HTTP dentro de `@Transactional` sin recuperación persistida: el rollback local no revierte cambios remotos.
- Transacciones distribuidas: requerirían soporte y operación adicionales que no existen en el proyecto actual.
- Orquestación o coreografía mediante eventos: opciones por concretar dentro de la coordinación propuesta, según contratos y responsabilidades del equipo.

## Consecuencias previstas

Habrá consistencia eventual y operaciones en estado intermedio; API, clientes, observabilidad y pruebas deberán contemplarlas. Se necesitarán recuperación tras reinicio y pruebas de respuestas perdidas, duplicados, timeouts, compensaciones fallidas y reconciliación. La outbox local ayuda a comunicar cambios, pero por sí sola no resuelve la saga.

## Verificación y seguimiento

Punto de partida: [límites de integración del servicio](../../services/rides/README.md#limite-de-integracion-de-esta-iteracion) y [MobilityGateway](../../services/rides/src/main/java/com/urbanmove/rides/MobilityGateway.java). No existe implementación ni verificación de esta propuesta. Resolver los acuerdos pendientes antes de sustituir la demo; registrar la aceptación y actualizar contratos, arquitectura y pruebas con la implementación.
