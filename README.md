# URBANMOVE - Monorepo Híbrido

Sistema distribuido de movilidad urbana basado en arquitectura de microservicios orientada a eventos (*Event-Driven Architecture*).

---

## 🏗️ Estructura del Monorepo

```plaintext
urbanmove/
├── clients/                 # Interfaces: Usuarios, Mantenimiento, Dashboard
│   ├── users/               # App Móvil / Web para usuarios
│   ├── maintenance/         # App para operarios y estado de recursos
│   └── dashboard/           # Panel administrativo y analítica
├── services/                # Backend Transaccional (Java 17 + Maven)
│   ├── reservations/        # Microservicio de Gestión de Reservas (Spring Boot 3)
│   ├── identity/            # Microservicio de Identidad y Autenticación
│   ├── rides/               # Microservicio de Gestión de Viajes
│   └── ...                  # 15 microservicios con aislamiento total
├── lambdas/                 # Procesamiento Asíncrono y Serverless (Python 3 + Boto3)
│   ├── reservation_expiry/  # Función periódica para liberación de reservas caducadas
│   ├── lake_ingestion/      # Ingesta de eventos hacia S3 Data Lake (capa raw)
│   └── data_marts/          # Agregados analíticos en capas trusted y curated
├── infra/                   # Infraestructura
│   └── init.sql             # Inicialización de las 15 bases de datos PostgreSQL aisladas
├── docs/                    # Documentación de arquitectura
│   ├── c4/                  # Diagramas C4 (Contenedores, Contexto)
│   └── contracts/           # Contratos OpenAPI y AsyncAPI
├── scripts/                 # Scripts de utilidad y automatización
│   └── InitEnv.java         # Generador del archivo .env a partir de .env.example
├── docker-compose.yml       # Orquestador local (PostgreSQL, RabbitMQ, MinIO, APIs)
├── pom.xml                  # POM padre exclusivo para la carpeta services/
├── .env.example             # Plantilla de variables de entorno locales
└── README.md                # Guía unificada de ejecución y desarrollo
```

---

## 🔒 Reglas Arquitectónicas Estrictas

1. **Aislamiento de Datos (Database-per-Service)**:
   - 15 microservicios con **15 bases de datos PostgreSQL independientes** (`urbanmove_identity`, `urbanmove_reservations`, etc.).
   - Están **estrictamente prohibidos los JOINs** entre bases de datos de distintos microservicios y las escrituras cruzadas.
2. **Monorepo Híbrido**:
   - `services/`: Java 17 + Maven (Spring Boot 3 transaccional, APIs REST, dominio).
   - `lambdas/`: Python 3 con `boto3` (Serverless, S3 Data Lake, SQS/EventBridge).
3. **Comunicación Asíncrona (Event-Driven)**:
   - Integración entre dominios mediante el bus de eventos RabbitMQ (Exchange: `urbanmove.events`) y AWS SQS.
4. **Data Lake**:
   - Sin BD relacional para analítica; los eventos se ingieren hacia Amazon S3 en capas `raw/`, `trusted/` y `curated/`.

---

## 🚲 Microservicio de Gestión de Reservas (`services/reservations`)

### Historia de Usuario
> *Como usuario de la aplicación, quiero reservar una bicicleta o patinete para garantizar que nadie más lo use antes de mi llegada, pudiendo confirmarla al iniciar el viaje o cancelarla si cambio de planes.*

### Criterios de Aceptación y Control de Concurrencia
1. **Solo recursos en estado `AVAILABLE`**: Se valida antes de permitir cualquier reserva.
2. **Transición a estado `RESERVED`**: Al crearse la reserva con éxito, el recurso cambia a `RESERVED` con un TTL configurable (por defecto 15 minutos).
3. **Control Estricto de Concurrencia**:
   - **Bloqueo Pesimista en JPA**: `findByIdWithLock(vehicleId)` ejecuta `SELECT ... FOR UPDATE`, serializando las peticiones sobre el mismo recurso.
   - **Índice Único Parcial Físico (PostgreSQL)**:
     ```sql
     CREATE UNIQUE INDEX uq_active_reservation_resource 
     ON reservations (resource_id) 
     WHERE status = 'PENDING';
     ```
   - **Criterio de Usuario Único Activo**:
     ```sql
     CREATE UNIQUE INDEX uq_active_user_reservation 
     ON reservations (user_id) 
     WHERE status = 'PENDING';
     ```
   - Garantiza que solicitudes simultáneas sobre el mismo vehículo otorgan la reserva a **un único cliente** y rechazan las demás con `409 Conflict`.

### Endpoints REST (Puerto 8106)

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/reservations` | Crea una reserva sobre un vehículo disponible. |
| `GET` | `/api/v1/reservations/{id}` | Consulta una reserva por su ID. |
| `GET` | `/api/v1/reservations?userId={id}` | Lista las reservas de un usuario. |
| `POST` | `/api/v1/reservations/{id}/confirm` | Confirma la reserva e inicia el viaje (Vehículo pasa a `IN_USE`). |
| `POST` | `/api/v1/reservations/{id}/cancel` | Cancela la reserva voluntariamente (Vehículo pasa a `AVAILABLE`). |
| `POST` | `/api/v1/internal/reservations/expire-timed-out` | Endpoint interno para la Lambda de expiración. Libera reservas vencidas. |
| `GET` | `/api/v1/vehicles` | Consulta la flota y su disponibilidad en este bounded context. |

### Eventos de Dominio Publicados (`urbanmove.events`)
- **`VehicleReserved`**: Emitido cuando la reserva se crea exitosamente.
- **`ReservationCancelled`**: Emitido cuando el usuario cancela su reserva voluntariamente.
- **`ReservationExpired`**: Emitido cuando la reserva es liberada por timeout/Lambda.

---

## 🚀 Despliegue y Ejecución Local

### 1. Variables de Entorno
Copia el archivo de variables de entorno de ejemplo antes de levantar los contenedores:
```bash
cp .env.example .env
```

### 2. Iniciar Infraestructura con Docker Compose
```bash
docker compose up -d
```
Esto levantará de forma orquestada:
- **PostgreSQL 16** (`:5432`): Inicializa automáticamente las 15 bases de datos aisladas (`urbanmove_identity`, `urbanmove_reservations`, etc.) definidas en `infra/init.sql`.
- **RabbitMQ** (`:5672` y `:15672`): Broker AMQP con panel web en [http://localhost:15672](http://localhost:15672) (Usuario/Clave: `urbanmove` / `urbanmove_broker_pass`).
- **MinIO** (`:9000` y `:9001`): S3 local con consola web en [http://localhost:9001](http://localhost:9001) (Usuario/Clave: `minioadmin` / `minioadminpassword`) y creación automática del bucket `urbanmove-lake` con capas `raw/`, `trusted/` y `curated/`.
- **Reservations Service** (`:8106`): API de reservas expuesta en [http://localhost:8106](http://localhost:8106).

### 3. Swagger UI / Documentación OpenAPI
Accede a la documentación interactiva en:
[http://localhost:8106/swagger-ui.html](http://localhost:8106/swagger-ui.html)


## Microservicio de Viajes [US-08] (`services/rides`)

La primera iteración de Viajes está disponible como **demo local independiente**, con inicio, consulta y finalización de viajes, idempotencia, coste preliminar y eventos mediante outbox. Disponibilidad, Reservas y Zonas se simulan dentro del perfil `demo`; todavía no se comunica con el servicio de Reservas del entorno común.

Esta entrega conserva su compilación independiente con **Java 21**, Spring Boot 3.5 y Maven Wrapper. El POM raíz y el servicio de Reservas mantienen su configuración Java 17. Viajes no está añadido al reactor Maven ni al Compose común; integrar esas decisiones queda pendiente.

Desde la raíz, para ejecutar exclusivamente la demo:

```powershell
docker compose -f compose.rides.yaml up --build -d
```

Página de bienvenida: `http://localhost:8082/`; contrato: `http://localhost:8082/openapi.yaml`. El puerto 8082 pertenece a esta demo; el puerto previsto de Viajes en el entorno común es 8107. El Compose de la demo publica RabbitMQ Management en 15672, por lo que no debe ejecutarse simultáneamente con el entorno común usando esos mismos puertos.

El comando general `docker compose up -d` sigue utilizando `docker-compose.yml` y la infraestructura del equipo. `compose.rides.yaml` se selecciona siempre explícitamente para evitar sustituir ese entorno.

- [Guía de Viajes, pruebas y solución de problemas](services/rides/README.md).
- [Cierre de la primera iteración](services/rides/docs/iteracion-1.md).
- [Arquitectura de la demo de Viajes](services/rides/docs/arquitectura.md).
- [Índice de documentación](docs/README.md) y [registro ADR](docs/adr/README.md).

Los cambios de comportamiento, contratos y decisiones deben actualizar los documentos afectados en la misma revisión.
