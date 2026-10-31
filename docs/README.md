# Documentación de UrbanMove

Revisión inicial: 2026-10-31. Esta documentación describe el estado del repositorio; no presupone que los servicios previstos estén implementados.

## Dónde consultar cada cosa

| Documento | Contenido |
| --- | --- |
| [README raíz](../README.md) | Entrada al proyecto y arranque con Compose |
| [README de Viajes](../services/rides/README.md) | Ejecución, API, ejemplos, pruebas y operación |
| [Cierre de la primera iteración](../services/rides/docs/iteracion-1.md) | Alcance entregado, verificaciones observadas y pendientes |
| [Arquitectura global](arquitectura.md) | Contexto del monorepo y entorno común |
| [Arquitectura de Viajes](../services/rides/docs/arquitectura.md) | Componentes, datos, flujos, límites y pendientes de la demo |
| [Registro ADR](adr/README.md) | Decisiones, motivación, alternativas y consecuencias |
| [OpenAPI](../services/rides/src/main/resources/static/openapi.yaml) | Contrato HTTP de Viajes |
| [Esquema de eventos](../services/rides/docs/events.schema.json) | Contrato de RideStarted y RideFinished |

## Documentar de forma incremental

En cada cambio, revisar qué comportamiento, contrato o decisión cambia y actualizar únicamente los documentos afectados en la misma revisión del código.

| Cambio | Documentación que se debe revisar |
| --- | --- |
| Endpoints, validación o respuestas | OpenAPI y README del servicio |
| Eventos o compatibilidad entre servicios | Esquema de eventos, consumidores afectados y ADR si cambia la estrategia |
| Persistencia, transacciones, integración o tecnología | Migraciones/configuración, arquitectura y ADR |
| Ejecución, variables o despliegue | README del servicio y Compose/CI correspondientes |
| Restricción resuelta o nueva limitación | Arquitectura y ADR afectado |

Los contratos permanecen junto al servicio. La documentación global enlaza esas fuentes para evitar mantener copias divergentes. Un ADR registra una decisión significativa; una corrección pequeña o una nueva ruta compatible no requieren automáticamente otro ADR.

## Comprobaciones de una revisión

- Los enlaces relativos y los comandos corresponden a archivos y configuración existentes.
- Lo implementado está separado de las propuestas y de los servicios simulados.
- Cada decisión arquitectónica nueva incluye contexto, alternativas, consecuencias y estado.
- Se indica qué validaciones se han ejecutado; disponer de una prueba o un workflow no equivale a haberlo ejecutado con éxito.

La revisión inicial se realizó leyendo código, configuración, migraciones, contratos y pruebas. No acredita por sí sola una ejecución del sistema ni del pipeline.
