-- ============================================================================
-- URBANMOVE - Inicialización de Infraestructura PostgreSQL
-- Aislamiento estricto de Datos: 15 Microservicios = 15 Bases de Datos Oficiales
-- ============================================================================

-- 1. [US-01] Microservicio de Identidad (8100)
CREATE DATABASE identity_db;

-- 2. [US-02] Microservicio de Perfiles de Usuario (8101)
CREATE DATABASE profiles_db;

-- 3. [US-03] Microservicio de Catálogo de Zonas (8102)
CREATE DATABASE zones_db;

-- 4. [US-04] Microservicio de Inventario de Recursos (8103)
CREATE DATABASE inventory_db;

-- 5. [US-05] Microservicio de Disponibilidad (8104)
CREATE DATABASE availability_db;

-- 6. [US-06] Microservicio de Batería y Telemetría (8105)
CREATE DATABASE telemetry_db;

-- 7. [US-07] Microservicio de Reservas (8106)
CREATE DATABASE reservations_db;

-- 8. [US-08] Microservicio de Viajes (8107)
CREATE DATABASE rides_db;

-- 9. [US-09] Microservicio de Incidencias (8108)
CREATE DATABASE incidents_db;

-- 10. [US-10] Microservicio de Órdenes de Mantenimiento (8109)
CREATE DATABASE maintenance_db;

-- 11. [US-11] Microservicio de Órdenes de Recolocación (8110)
CREATE DATABASE relocations_db;

-- 12. [US-12] Microservicio de Adquisiciones (8111)
CREATE DATABASE acquisitions_db;

-- 13. [US-13] Microservicio de Historial (8112)
CREATE DATABASE history_db;

-- 14. [US-14] Microservicio de Notificaciones (8113)
CREATE DATABASE notifications_db;

-- 15. [US-15] Microservicio de Analítica Operacional (8114)
CREATE DATABASE analytics_db;

-- ============================================================================
-- Esquema y Tablas para el Microservicio de Reservas (reservations_db)
-- ============================================================================
\connect reservations_db;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de Recursos / Vehículos (proyección local de disponibilidad y bloqueo en reservas)
-- 9 estados oficiales: AVAILABLE, RESERVED, IN_USE, LOW_BATTERY, WITHDRAWN, CHARGING, REPAIRING, READY_FOR_SERVICE, RETIRED
CREATE TABLE IF NOT EXISTS vehicles (
    id VARCHAR(64) PRIMARY KEY,
    resource_type VARCHAR(20) NOT NULL, -- BIKE, SCOOTER
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Reservas
-- Estados oficiales: PENDING, CONFIRMED, CANCELLED, EXPIRED
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    resource_id VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, CONFIRMED, CANCELLED, EXPIRED
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at TIMESTAMP WITHOUT TIME ZONE,
    cancellation_reason VARCHAR(255),
    correlation_id VARCHAR(64),
    CONSTRAINT fk_reservation_vehicle FOREIGN KEY (resource_id) REFERENCES vehicles(id)
);

-- Control de Concurrencia Crítico 1:
-- Garantiza que NUNCA pueda existir más de una reserva activa (PENDING) sobre el mismo vehículo.
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_reservation_resource 
ON reservations (resource_id) 
WHERE status = 'PENDING';

-- Criterio de Aceptación Crítico 2:
-- "Un usuario no puede tener más de una reserva activa a la vez"
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_user_reservation 
ON reservations (user_id) 
WHERE status = 'PENDING';

-- Índices de consulta y proceso de expiración
CREATE INDEX IF NOT EXISTS idx_reservations_status_expires 
ON reservations (status, expires_at);

CREATE INDEX IF NOT EXISTS idx_reservations_user 
ON reservations (user_id);

-- Semilla inicial de flota para pruebas y entorno de desarrollo
INSERT INTO vehicles (id, resource_type, status, version, created_at, updated_at)
VALUES 
    ('BIKE-001', 'BIKE', 'AVAILABLE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BIKE-002', 'BIKE', 'AVAILABLE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SCOOTER-001', 'SCOOTER', 'AVAILABLE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SCOOTER-002', 'SCOOTER', 'AVAILABLE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SCOOTER-003', 'SCOOTER', 'LOW_BATTERY', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BIKE-003', 'BIKE', 'REPAIRING', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
