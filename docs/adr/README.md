# Registro de decisiones arquitectónicas (ADR)

Un ADR conserva el contexto, la decisión, sus alternativas y consecuencias. Este registro empieza el 2026-10-31 con una reconstrucción de las decisiones visibles en el código y en el README; no atribuye al equipo debates ni fechas de aprobación que no constan.

## Decisiones

| ADR | Decisión | Estado |
| --- | --- | --- |
| [0001](0001-servicio-viajes-y-persistencia.md) | Servicio Viajes con Spring Boot y persistencia relacional propia | Aceptado (implementado; registro retrospectivo) |
| [0002](0002-puerto-movilidad-y-demo.md) | Puerto de movilidad y adaptador demo aislado por perfil | Aceptado (implementado; registro retrospectivo) |
| [0003](0003-idempotencia-y-concurrencia.md) | Idempotencia y exclusión concurrente en base de datos | Aceptado (implementado; registro retrospectivo) |
| [0004](0004-outbox-y-eventos.md) | Outbox transaccional y publicación RabbitMQ al menos una vez | Aceptado (implementado; registro retrospectivo) |
| [0005](0005-tarifa-conservada-por-viaje.md) | Conservar la tarifa aplicada al iniciar cada viaje | Aceptado (implementado; registro retrospectivo) |
| [0006](0006-integracion-remota-coordinada.md) | Coordinación persistente para integrar servicios remotos | Propuesto (no implementado) |

## Cómo mantener el registro

1. Crear `NNNN-titulo-breve.md` con el siguiente número libre y la [plantilla](plantilla.md). No reutilizar números.
2. Añadir la entrada a este índice y enlazar código, contratos y ADR relacionados.
3. Empezar como **Propuesto** cuando la elección esté pendiente. Pasar a **Aceptado** cuando el equipo la acuerde; anotar fecha y evidencia disponible sin inventar participantes.
4. Si se descarta, marcar **Rechazado** con el motivo. Si otra decisión la reemplaza, conservar el ADR anterior como **Sustituido por ADR-NNNN** y añadir enlaces en ambos sentidos.
5. Conservar el contexto histórico. Se pueden corregir erratas o añadir aclaraciones fechadas; un cambio sustancial de estrategia se registra en un nuevo ADR.

Registrar elecciones que afecten límites de servicios, propiedad de datos, consistencia, contratos, seguridad, tecnología o despliegue. Incluir consecuencias negativas y condiciones que obligarían a revisar la decisión. Los comandos de operación y el detalle de endpoints permanecen en el README y los contratos.
