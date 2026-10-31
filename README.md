# UrbanMove

Primera iteracion: microservicio Java **Viajes** en [`services/rides`](services/rides/README.md).

Iteración 1 cerrada como demo local el 2026-10-31. Consulta el [alcance, evidencias y pendientes](services/rides/docs/iteracion-1.md).

```powershell
docker compose up --build -d
```

Pagina de bienvenida: `http://localhost:8082/`; API de viajes: `http://localhost:8082/rides`; contrato: `http://localhost:8082/openapi.yaml`.

Para ejecutar sin Docker, ejemplos de uso, pruebas y limites del modo demo, consulta el README del servicio. Disponibilidad y Reservas estan simulados hasta integrar los microservicios del equipo.

## Documentación del proyecto

- [Índice y mantenimiento de la documentación](docs/README.md).
- [Arquitectura actual y límites de integración](docs/arquitectura.md).
- [Registro de decisiones arquitectónicas (ADR)](docs/adr/README.md).

Cada cambio relevante debe actualizar su documentación y, si introduce o modifica una decisión arquitectónica, incluir un ADR en la misma revisión.
