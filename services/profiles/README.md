# [US-02] Microservicio de Perfiles de Usuario (`profiles`)

- **Puerto de desarrollo**: 8101
- **Base de datos**: `profiles_db` (PostgreSQL)
- **Responsabilidad**: Gestión de datos de contacto del usuario (nombre, correo, teléfono, preferencias de transporte, bajas lógicas).
- **Restricción explícita**: No emite tokens ni valida sesiones; consume el identificador de usuario validado previamente por `identity`.
