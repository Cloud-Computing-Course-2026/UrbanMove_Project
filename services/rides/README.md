# UrbanMove: Viajes

Primera iteracion del microservicio `rides`: Java 21, Spring Boot, Maven Wrapper, JPA, Flyway, H2/PostgreSQL y RabbitMQ. Implementa inicio, consulta y fin de viajes, calculo de duracion y coste preliminar, idempotencia y outbox transaccional.

La [arquitectura global](../../docs/arquitectura.md) describe los límites actuales. El [registro ADR](../../docs/adr/README.md) conserva las decisiones implementadas y las propuestas de integración pendientes. Consulta la [guía de documentación](../../docs/README.md) al modificar comportamiento, contratos o arquitectura.

## Estado de la iteración

Primera iteración **cerrada como demo local el 2026-10-31**. El [registro de cierre](docs/iteracion-1.md) recoge el alcance entregado, las evidencias de verificación y los pendientes. No implica integración con servicios reales ni preparación para producción.

## Estructura del código

`RidesApplication` permanece en `com.urbanmove.rides`, como raíz del escaneo de componentes, entidades y repositorios de Spring Boot. Las clases se distribuyen por responsabilidad:

| Paquete bajo `com.urbanmove.rides` | Clases / responsabilidad |
| --- | --- |
| `controller` | `RideController` y `ApiErrors`: HTTP y respuestas de error |
| `dto` | `RideApi`: peticiones y respuestas de la API |
| `service` | `RideService`: coordinación, reglas y transacciones |
| `model` | `Ride`: entidad JPA con campos privados, creación y finalización mediante métodos |
| `repository` | `RideRepository`: consultas y bloqueo de viajes |
| `exception` | `DomainException`: errores de negocio |
| `integration` y `integration.demo` | `MobilityGateway` y `DemoMobilityGateway`: puerto de movilidad y simulación |
| `messaging` | `EventTransport` y `RabbitEvents`: transporte y configuración RabbitMQ |
| `messaging.outbox` | `Outbox` y `OutboxPublisher`: almacenamiento y publicación de eventos |
| `observability` | `CorrelationFilter` y `RideMetrics`: correlación y métricas |

Los recursos siguen en `src/main/resources`: configuración YAML, migraciones en `db/` y bienvenida/contrato en `static/`. Las pruebas de integración permanecen en `src/test/java/com/urbanmove/rides`, porque verifican el servicio completo. Esta distribución no cambia las rutas HTTP, el JSON ni el esquema SQL. Véase [ADR-0007](../../docs/adr/0007-organizacion-paquetes.md).

## Ejecutar sin Docker

Desde `services/rides`, con un **JDK 21** y acceso a Maven Central en la primera ejecucion:

```powershell
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

En Linux/macOS: `bash mvnw verify` y `bash mvnw spring-boot:run`.

Escucha en `http://localhost:8082`. La ruta `/` muestra una pagina de bienvenida con las operaciones disponibles y enlaces al contrato OpenAPI y al estado del servicio. Para iniciar un viaje se utiliza `POST /rides`; no existe un listado en `GET /rides`. Por defecto utiliza perfil `demo`, H2 persistente en `data/` y deja los eventos pendientes en la outbox, sin necesitar RabbitMQ. Reiniciar conserva los viajes y los vehiculos. No hay consola H2 ni credenciales de cloud.

## Ejecutar con Docker

Desde la raiz del repositorio, con Docker Engine activo:

```powershell
docker compose up --build -d
docker compose logs -f rides
```

Levanta el servicio, su PostgreSQL y RabbitMQ con volumenes persistentes y health checks. Las credenciales predeterminadas de Compose son exclusivamente de demostracion local; se pueden cambiar mediante `RIDES_DB_PASSWORD` y `RABBITMQ_PASSWORD`. Los puertos publicados estan limitados a localhost. `docker compose down` detiene sin borrar los datos.

## Probar un viaje (PowerShell)

```powershell
$base = 'http://localhost:8082'
$startKey = [guid]::NewGuid().ToString()
$flow = [guid]::NewGuid().ToString()
$headers = @{ 'Idempotency-Key' = $startKey; 'X-Correlation-ID' = $flow }
$body = @{
    userId = '30000000-0000-0000-0000-000000000001'
    vehicleId = '10000000-0000-0000-0000-000000000001'
} | ConvertTo-Json
$ride = Invoke-RestMethod "$base/rides" -Method Post -Headers $headers -ContentType 'application/json' -Body $body
Invoke-RestMethod "$base/rides/$($ride.id)"
$headers['Idempotency-Key'] = [guid]::NewGuid().ToString()
$end = @{ destinationZoneId = '00000000-0000-0000-0000-000000000002' } | ConvertTo-Json
Invoke-RestMethod "$base/rides/$($ride.id)/finish" -Method Post -Headers $headers -ContentType 'application/json' -Body $end
```

### Comprobar la idempotencia de finalización

En la misma sesión de PowerShell, después del ejemplo anterior, ejecuta únicamente:

```powershell
Invoke-RestMethod "$base/rides/$($ride.id)/finish" -Method Post -Headers $headers -ContentType 'application/json' -Body $end
```

No regeneres la clave ni cambies el destino: se deben conservar el identificador, `finishedAt`, `durationSeconds` y `preliminaryCost`. Una clave diferente o un destino distinto en un viaje finalizado devuelve `409` con el detalle `El viaje ya ha finalizado`. La respuesta HTTP por sí sola no demuestra la ausencia de eventos duplicados; las pruebas automáticas comprueban también la outbox.

Con Compose puedes inspeccionar los eventos del viaje desde la raíz del repositorio (requiere conservar `$ride` en la sesión):

```powershell
docker compose exec rides-db psql -U rides -d rides -c "SELECT event_type, COUNT(*) FROM outbox_events WHERE payload::jsonb->'data'->>'id' = '$($ride.id)' GROUP BY event_type;"
```

Tras iniciar, finalizar y repetir la finalización, debe haber un `RideStarted` y un `RideFinished`. Esta consulta inspecciona registros de la outbox, no el número de entregas al broker: la entrega sigue siendo al menos una vez.

Guarda las claves para repetir peticiones: repetir el mismo inicio no crea otro viaje; repetir el fin con la misma clave/destino no genera otro evento. La respuesta de inicio repetido es `201` con el estado **actual** del viaje y la misma `Location`. Un conflicto concurrente puede devolver `409`; despues de resolverse puede reintentarse con la misma clave. Usar otra carga con una clave ya utilizada devuelve `409`. Las claves de inicio y fin tienen espacios separados y unicidad global dentro de cada operacion.

### Datos de demostracion

| Entidad | UUID | Estado |
| --- | --- | --- |
| Usuario de la reserva | `30000000-0000-0000-0000-000000000001` | Usuario de prueba |
| Bicicleta | `10000000-0000-0000-0000-000000000001` | Disponible en Triana |
| Patinete reservado | `10000000-0000-0000-0000-000000000002` | Reservado por el usuario anterior |
| Patinete sin bateria | `10000000-0000-0000-0000-000000000003` | No utilizable |
| Reserva del patinete | `20000000-0000-0000-0000-000000000001` | Confirmada, caduca el 01/01/2099 (dato fijo demo) |
| Triana | `00000000-0000-0000-0000-000000000001` | Aparcamiento permitido |
| Las Canteras | `00000000-0000-0000-0000-000000000002` | Aparcamiento permitido |
| Zona restringida | `00000000-0000-0000-0000-000000000003` | Aparcamiento prohibido |

Para usar el patinete reservado, envia tambien `reservationId`. La simulacion acepta UUID de usuario sin consultar Identidad/Perfiles. Rechaza reservas ajenas, caducadas, consumidas o de otro vehiculo. La expiracion automatica de reservas queda fuera de Viajes.

La tabla describe los datos iniciales. Después de finalizar un viaje, el vehículo permanece en el destino y ese será su siguiente origen. Reiniciar no restablece estos datos; la reserva consumida tampoco vuelve a estar disponible. Docker usa PostgreSQL y el arranque local predeterminado usa H2: son almacenes independientes.

## API y reglas

Contrato OpenAPI: [src/main/resources/static/openapi.yaml](src/main/resources/static/openapi.yaml), tambien en `GET /openapi.yaml`.

| Metodo | Ruta | Resultado |
| --- | --- | --- |
| POST | `/rides` | `201` viaje iniciado |
| POST | `/rides/{id}/finish` | `200` viaje finalizado |
| GET | `/rides/{id}` | `200` estado del viaje |

Los POST requieren `Idempotency-Key` de 1-128 caracteres alfanumericos o `._:-`. Errores `400` para entrada invalida, `404` para recurso inexistente, `409` para reglas de negocio o conflictos. Respuestas de error `application/problem+json` con `correlationId`.

- Estados del viaje: `IN_PROGRESS -> FINISHED`. Los estados del vehiculo pertenecen a Disponibilidad.
- Una sola carrera activa por vehiculo y por usuario; restricciones unicas en BD, no bloqueos en memoria. Finalizacion protegida con bloqueo de fila.
- El origen y tipo provienen de Disponibilidad, nunca de datos arbitrarios enviados por el cliente.
- Precio EUR: desbloqueo `0.50` + minutos empezados (minimo 1) x `0.10` bicicleta / `0.20` patinete. La tarifa se copia al viaje al iniciarlo, evitando que un cambio posterior altere el coste. Duracion en segundos enteros; para facturar se conserva la fraccion temporal. Ejemplo: bicicleta 61 s = `0.70 EUR`.
- El coste es preliminar: no hay cobros reales ni servicio de facturacion.
- No implementa el historial global: Historial consumira eventos y mantendra su propia proyeccion.

## Eventos y tolerancia a fallos

`RideStarted` y `RideFinished` llevan un snapshot del viaje; [docs/events.schema.json](docs/events.schema.json) define el contrato. Productor `rides`; consumidores previstos: Disponibilidad, Historial, notificaciones e ingesta analitica. Exchange durable `urbanmove.events`, routing keys exactamente iguales a los nombres de evento. Cola durable de inspeccion `urbanmove.rides.events`. **Cada consumidor necesita su propia cola y bindings**; no deben competir por la cola de inspeccion.

Viaje y evento se guardan en una misma transaccion. El publicador lee la outbox con bloqueo de BD, envia mensajes persistentes y solo marca publicado tras confirmacion del broker y ausencia de devolucion por falta de ruta. Ante fallo/timeout, registra intentos y reintenta con espera exponencial de hasta 60 s. No descarta eventos; los fallidos quedan visibles en la outbox. No hay DLQ automatica en esta primera iteracion.

Entrega **al menos una vez**: una caida tras enviar y antes de marcar puede duplicar eventos. Consumidores deben deduplicar por `eventId`. No se garantiza orden global; utilizar tipo, id de viaje y timestamps del snapshot para no regresar estados. La tabla tiene un indice para seleccionar pendientes; retencion/archivado de publicados queda para otra iteracion. Cada tick publica como maximo un evento: es suficiente para la demo, pero debe cambiarse por lotes/particiones si aumenta la carga.

### Escenario de fallo reproducible con Compose

1. Inicia los contenedores y un viaje con el ejemplo anterior.
2. Ejecuta `docker compose stop rabbitmq`.
3. Finaliza el viaje: debe funcionar y dejar el evento pendiente, aunque falle RabbitMQ.
4. Consulta pendientes: `docker compose exec rides-db psql -U rides -d rides -c "SELECT event_id, event_type, attempts, last_error FROM outbox_events WHERE published_at IS NULL;"`.
5. Ejecuta `docker compose start rabbitmq`; espera al siguiente reintento y consulta de nuevo. Los mensajes aparecen en RabbitMQ Management (`http://localhost:15672`, usuario `urbanmove`, clave local configurada).

Las pruebas automaticas cubren broker indisponible con transporte simulado y recuperacion, ademas de conflictos concurrentes; el escenario anterior comprueba el broker real.

## Limite de integracion de esta iteracion

`MobilityGateway` es el puerto de integracion. `DemoMobilityGateway` existe **solo en perfil demo** y simula Disponibilidad, Reservas y Zonas mediante tablas `demo_*`, dentro de la misma transaccion de pruebas. Las tablas no son el inventario real ni deben compartirse con otros microservicios. Sin ese perfil, el servicio falla al arrancar hasta que se aporte un adaptador real: no hay aceptacion silenciosa de vehiculos arbitrarios.

Antes de conectar los servicios reales, acordad:

1. **Claim atomico e idempotente**, identificado por `rideId`: validar disponibilidad/reserva propia y marcar `IN_USE` como una operacion coordinada. Un simple GET seguido de un PUT no evita carreras.
2. **Finalizacion idempotente** por `rideId`: validar destino y liberar o cambiar estado segun bateria/mantenimiento.
3. No trasladar directamente las llamadas de la demo a HTTP dentro de la transaccion: servicios remotos no participan en el commit local. Implementar una saga persistente con estados intermedios y compensacion/reconciliacion para claims y liberaciones, con timeouts y reintentos. Acordar quien consume la reserva y quien actualiza disponibilidad al recibir `RideFinished`.
4. Obtener usuario y permisos del token emitido por Identidad; proteger consulta/finalizacion por propietario. Actualmente no hay autenticacion y la API es exclusivamente una demo local.

## Observabilidad y configuracion

- `GET /actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`.
- `GET /actuator/prometheus`: `rides_active`, `rides_outbox_pending`, contadores de operaciones/eventos y metricas HTTP/JVM.
- Logs JSON con `correlationId` en HTTP y `eventId` en publicacion; el payload de evento conserva la correlacion completa.
- Con RabbitMQ caido, el health global puede devolver `503`, pero liveness sigue activo y los endpoints de viajes funcionan.

Variables: `PORT` (8082), `SERVER_ADDRESS` (127.0.0.1), `DB_URL`, `DB_USER`, `DB_PASSWORD`, `RIDES_EVENTS_ENABLED` (false), `RABBITMQ_HOST/PORT/USER/PASSWORD`, `UNLOCK_FEE`, `BICYCLE_PER_MINUTE`, `SCOOTER_PER_MINUTE`.

## Pruebas y CI

`mvnw verify` valida estilo basico, ejecuta integracion HTTP/BD y construye el JAR. Las pruebas incluyen persistencia, idempotencia, precio, reservas, rollback, zonas, carreras concurrentes y recuperacion de outbox. Normalmente usan H2; `TEST_DB_URL`, `TEST_DB_USER` y `TEST_DB_PASSWORD` permiten ejecutarlas contra PostgreSQL dedicado (las pruebas borran datos de sus tablas).

El workflow `.github/workflows/rides.yml` ejecuta estas comprobaciones con H2 y PostgreSQL, verifica el transporte real con RabbitMQ, valida Compose, construye la imagen y publica informes/JAR.

## Solución de problemas locales

### Java incompatible: versiones de clase 65 y 61

Las clases para Java 21 usan versión 65; Java 17 solo admite hasta 61. Comprueba el JDK utilizado por Maven con `.\mvnw.cmd -version`. Debe indicar Java 21. En PowerShell, selecciona un JDK 21 instalado y recompila:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\jbr-21.0.9" # Ajustar a la instalación local
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -version
.\mvnw.cmd clean verify
```

El cambio afecta a esa terminal. Para hacerlo permanente, configura `JAVA_HOME` y `Path` en las variables de entorno de Windows.

### Puerto 8082 ocupado

No arranques Docker y Maven en el mismo puerto. Para pasar de Docker a Maven, ejecuta `docker compose stop rides` y después `.\mvnw.cmd spring-boot:run` desde `services/rides`. Para mantener ambas instancias, en PowerShell:

```powershell
$env:PORT = '8083'
.\mvnw.cmd spring-boot:run
```

Usa entonces `http://localhost:8083` en los ejemplos. Para volver al puerto predeterminado en esa terminal: `Remove-Item Env:PORT`.

### Conflictos 409

- `El viaje ya ha finalizado`: reintento con clave o destino distintos de la finalización original. Conserva ambos; si perdiste la clave, consulta el viaje y prueba la idempotencia con otro viaje.
- Viaje activo para usuario o vehículo: consulta y finaliza el viaje anterior antes de iniciar otro.
- Clave utilizada con otros datos: una clave identifica una operación concreta; reutilízala solo para repetir esa misma operación.

El cuerpo del error incluye `detail` y `correlationId`. Los comandos de prueba son para PowerShell; los backticks de continuación no son sintaxis de CMD.
