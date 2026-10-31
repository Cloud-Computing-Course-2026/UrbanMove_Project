# UrbanMove

Primera iteracion: microservicio Java **Viajes** en [`services/rides`](services/rides/README.md).

```powershell
docker compose up --build -d
```

Pagina de bienvenida: `http://localhost:8082/`; API de viajes: `http://localhost:8082/rides`; contrato: `http://localhost:8082/openapi.yaml`.

Para ejecutar sin Docker, ejemplos de uso, pruebas y limites del modo demo, consulta el README del servicio. Disponibilidad y Reservas estan simulados hasta integrar los microservicios del equipo.
