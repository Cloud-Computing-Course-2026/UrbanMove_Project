package com.urbanmove.rides;

import java.time.Instant;
import java.util.UUID;

/** Boundary to Availability, Reservations and Zones. The demo adapter participates in the local transaction. */
public interface MobilityGateway {
    record ClaimedVehicle(UUID originZoneId, Ride.VehicleType type) { }

    ClaimedVehicle claim(UUID rideId, UUID vehicleId, UUID userId, UUID reservationId, Instant now);

    void finish(UUID rideId, UUID vehicleId, UUID destinationZoneId);
}
