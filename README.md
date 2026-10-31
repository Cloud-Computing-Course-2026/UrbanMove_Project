# URBANMOVE - Monorepo Híbrido

Sistema distribuido de movilidad urbana basado en arquitectura de microservicios orientada a eventos (*Event-Driven Architecture*).

---

## 🏗️ Estructura del Proyecto

```plaintext
urbanmove/
├── clients/                 # Interfaces: Usuarios, Mantenimiento, Dashboard
│   ├── users/               # App Móvil / Web para usuarios
│   ├── maintenance/         # App para operarios y estado de recursos
│   └── dashboard/           # Panel administrativo y analítica
├── services/                # Backend Transaccional (Java 17 + Maven)
│   ├── reservations/        # Microservicio de Gestión de Reservas (Spring Boot 3)
│   ├── identity/            # Microservicio de Identidad y Autenticación
│   └── history/             # Microservicio de Histórico de Operaciones
├── lambdas/                 # Procesamiento Asíncrono y Serverless (Python 3 + Boto3)
│   ├── reservation_expiry/  # Función periódica para liberación de reservas caducadas
│   ├── lake_ingestion/      # Ingesta de eventos hacia S3 Data Lake (capa raw)
│   └── data_marts/          # Agregados analíticos en capas trusted y curated
├── infra/                   # Infraestructura
│   └── init.sql             # Inicialización de las 15 bases de datos PostgreSQL aisladas
├── docs/                    # Documentación de arquitectura
│   ├── c4/                  # Diagramas C4 (Contenedores, Contexto)
│   └── contracts/           # Contratos OpenAPI y AsyncAPI
├── docker-compose.yml       # Orquestador local (PostgreSQL, RabbitMQ, MinIO, APIs)
├── pom.xml                  # POM padre exclusivo para la carpeta services/
└── README.md                # Guía unificada de ejecución y desarrollo
```

---

## 🔒 Reglas Arquitectónicas Estrictas

1. **Aislamiento de Datos**:
   - 15 microservicios con **15 bases de datos PostgreSQL independientes**.
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
     CREATE UNIQUE INDEX uq_active_reservation_vehicle 
     ON reservations (vehicle_id) 
     WHERE status = 'RESERVED';
     ```
   - Garantiza que solicitudes simultáneas sobre el mismo vehículo otorgan la reserva a **un único cliente** y rechazan las demás con `409 Conflict`.

### Endpoints REST

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

### 1. Iniciar Infraestructura con Docker Compose
```bash
docker compose up -d
```
Esto levantará:
- **PostgreSQL 16**: Inicializa automáticamente las 15 bases de datos aisladas (`reservations_db`, `identity_db`, etc.) definidas en `infra/init.sql`.
- **RabbitMQ**: Broker AMQP con panel web en [http://localhost:15672](http://localhost:15672) (Usuario/Clave: `urbanmove` / `urbanmove_broker_pass`).
- **MinIO**: S3 local con consola en [http://localhost:9001](http://localhost:9001) (Usuario/Clave: `minioadmin` / `minioadminpassword`).
- **Reservations Service**: API de reservas expuesta en [http://localhost:8082](http://localhost:8082).

### 2. Swagger UI / Documentación OpenAPI
Accede a la documentación interactiva en:
[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
