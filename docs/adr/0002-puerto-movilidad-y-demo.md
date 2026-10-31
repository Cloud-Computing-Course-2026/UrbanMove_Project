# ADR-0002: Puerto de movilidad y adaptador demo

- Estado: Aceptado (implementado; registro retrospectivo)
- Fecha de registro: 2026-10-31
- Fecha de decisión original: No consta
- Alcance: Dependencias de Disponibilidad, Reservas y Zonas

## Contexto

Viajes necesita comprobar y reclamar vehículos, validar reservas y comprobar el destino, pero los servicios responsables todavía no están integrados.

## Decisión

Concentrar estas operaciones en `MobilityGateway` mediante `claim` y `finish`. El único adaptador actual, `DemoMobilityGateway`, se activa con el perfil `demo` y utiliza tablas `demo_*` que participan en la transacción local. Sin ese perfil ni un adaptador alternativo, la aplicación no puede arrancar. No se aceptan vehículos arbitrarios como alternativa silenciosa.

## Alternativas

Contraste retrospectivo: incrustar la simulación en `RideService` dificultaría sustituirla; aceptar cualquier UUID ocultaría errores de integración; conectar directamente otros servicios aún no disponibles impediría la demostración autónoma.

## Consecuencias

La demo es persistente y reproducible, pero sus garantías atómicas no representan una integración distribuida. Las tablas demo no son inventario compartido. Sustituir el adaptador requiere resolver consistencia, reintentos y compensaciones; véase [ADR-0006](0006-integracion-remota-coordinada.md).

## Verificación y seguimiento

Evidencia: [puerto](../../services/rides/src/main/java/com/urbanmove/rides/integration/MobilityGateway.java), [adaptador](../../services/rides/src/main/java/com/urbanmove/rides/integration/demo/DemoMobilityGateway.java) y [perfil](../../services/rides/src/main/resources/application-demo.yml). Las [pruebas existentes](../../services/rides/src/test/java/com/urbanmove/rides/RidesIntegrationTest.java) comprueban reservas, zonas y rollback. Revisar antes de integrar el primer servicio remoto.
