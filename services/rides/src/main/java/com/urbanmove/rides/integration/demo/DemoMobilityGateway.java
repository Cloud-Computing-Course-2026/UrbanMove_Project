package com.urbanmove.rides.integration.demo;

import com.urbanmove.rides.exception.DomainException;
import com.urbanmove.rides.integration.MobilityGateway;
import com.urbanmove.rides.model.Ride;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Persistent simulation only: not the real inventory/reservation services. */
@Component
@Profile("demo")
public class DemoMobilityGateway implements MobilityGateway {
    private final JdbcTemplate jdbc;

    public DemoMobilityGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ClaimedVehicle claim(UUID rideId, UUID vehicleId, UUID userId, UUID reservationId, Instant now) {
        var vehicles = jdbc.query("SELECT zone_id, vehicle_type, state FROM demo_vehicles WHERE id = ? FOR UPDATE",
                (rs, row) -> new Vehicle(rs.getObject(1, UUID.class),
                        Ride.VehicleType.valueOf(rs.getString(2)), rs.getString(3)), vehicleId);
        if (vehicles.isEmpty()) {
            throw DomainException.notFound("Vehiculo inexistente");
        }
        var vehicle = vehicles.getFirst();
        if (reservationId == null) {
            if (!"AVAILABLE".equals(vehicle.state())) {
                throw DomainException.conflict("El vehiculo no esta disponible");
            }
        } else {
            if (!"RESERVED".equals(vehicle.state())) {
                throw DomainException.conflict("El vehiculo no tiene una reserva utilizable");
            }
            int changed = jdbc.update("""
                    UPDATE demo_reservations SET state = 'CONSUMED'
                    WHERE id = ? AND vehicle_id = ? AND user_id = ?
                    AND state = 'CONFIRMED' AND expires_at > ?
                    """, reservationId, vehicleId, userId, Timestamp.from(now));
            if (changed != 1) {
                throw DomainException.conflict("Reserva inexistente, ajena, caducada o consumida");
            }
        }
        jdbc.update("UPDATE demo_vehicles SET state = 'IN_USE', ride_id = ? WHERE id = ?", rideId, vehicleId);
        return new ClaimedVehicle(vehicle.zone(), vehicle.type());
    }

    @Override
    public void finish(UUID rideId, UUID vehicleId, UUID destinationZoneId) {
        Integer allowed = jdbc.queryForObject("SELECT COUNT(*) FROM demo_zones WHERE id = ? AND parking_allowed = TRUE",
                Integer.class, destinationZoneId);
        if (allowed == null || allowed != 1) {
            throw DomainException.conflict("Zona de destino inexistente o aparcamiento no permitido");
        }
        int changed = jdbc.update("""
                UPDATE demo_vehicles SET zone_id = ?, state = 'AVAILABLE', ride_id = NULL
                WHERE id = ? AND state = 'IN_USE' AND ride_id = ?
                """, destinationZoneId, vehicleId, rideId);
        if (changed != 1) {
            throw DomainException.conflict("El vehiculo no pertenece a este viaje activo");
        }
    }

    private record Vehicle(UUID zone, Ride.VehicleType type, String state) { }
}
