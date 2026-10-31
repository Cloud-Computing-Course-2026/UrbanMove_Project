# Módulo Compartido (`shared`)

Librería común para los microservicios Java de `services/`:
- Formato estándar de eventos de dominio (`event_id`, `timestamp`, `source`, `event_type`, `schema_version`, `correlation_id`).
- Manejo uniforme de correlación (`correlation_id` en headers HTTP / AMQP).
- Clases base de excepciones de dominio y respuestas ProblemDetail/JSON estándar.
- **Nota**: No es un microservicio autónomo ni almacena datos operacionales.
