# Contexto del Proyecto: URBANMOVE

Estás asistiendo en el desarrollo de un sistema distribuido de movilidad urbana basado en una arquitectura de microservicios orientada a eventos.

## Estructura del Monorepo Híbrido (Java + Python)

```plaintext
urbanmove/
├── clients/                 # Interfaces: Usuarios, Mantenimiento, Dashboard
├── services/                # Backend Transaccional (Java 17 + Maven)
│   ├── identity/            # pom.xml, Dockerfile, src/main/java...
│   ├── reservations/        # pom.xml, Dockerfile, src/main/java...
│   └── history/             # pom.xml, Dockerfile, src/main/java...
├── lambdas/                 # Procesamiento Asíncrono y AWS (Python 3)
│   ├── reservation_expiry/  # handler.py, requirements.txt
│   ├── lake_ingestion/      # handler.py, requirements.txt
│   └── data_marts/          # handler.py, requirements.txt
├── infra/                   # init.sql (PostgreSQL), scripts Boto3/Terraform
├── docs/                    # Diagramas C4, ADRs, contratos OpenAPI/AsyncAPI
├── docker-compose.yml       # Orquestador (PostgreSQL, RabbitMQ, MinIO, APIs)
├── pom.xml                  # POM padre exclusivo para la carpeta services/
└── README.md                # Instrucciones para levantar el entorno unificado
```

## Pila Tecnológica y Reglas Estrictas

1. **Monorepo Híbrido**:
   - Todo el código dentro de `services/` son microservicios atómicos transaccionales desarrollados en **Java 17 con Maven**.
   - Todo el código dentro de `lambdas/` son funciones serverless y analíticas desarrolladas en **Python 3** utilizando la librería `boto3`.

2. **Aislamiento de Datos**:
   - Existen 15 microservicios en total.
   - Cada microservicio tiene su propia base de datos PostgreSQL exclusiva.
   - **Estrictamente prohibidos**:
     - Joins entre bases de datos de distintos microservicios.
     - Escrituras cruzadas directas entre bases de datos.

3. **Comunicación Asíncrona (Event-Driven)**:
   - La comunicación e integración entre dominios se realiza mediante la publicación y consumo de eventos de dominio en un bus de eventos (**RabbitMQ / AWS SQS**).

4. **Data Lake**:
   - No se utiliza una base de datos relacional para analítica.
   - Los eventos se ingieren mediante funciones Lambda (Python + Boto3) hacia un **Data Lake en Amazon S3**, estructurado en capas:
     - `raw/`
     - `trusted/`
     - `curated/`

5. **Criterios de Generación de Código**:
   - Endpoints REST, controladores, entidades o lógica de dominio transaccional: **Java 17 / Maven**.
   - Funciones serverless, interacción directa con AWS S3/SQS o scripts de despliegue cloud: **Python 3 / Boto3**.
   - Priorizar código modular, idempotente, desacoplado y tolerante a fallos.
