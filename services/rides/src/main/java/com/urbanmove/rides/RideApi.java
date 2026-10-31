package com.urbanmove.rides;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class RideApi {
    private RideApi() { }

    public record StartRequest(@NotNull UUID userId, @NotNull UUID vehicleId, UUID reservationId) { }
    public record FinishRequest(@NotNull UUID destinationZoneId) { }
    public record RideResponse(UUID id, UUID userId, UUID vehicleId, UUID reservationId,
            Ride.VehicleType vehicleType, Ride.Status status, UUID originZoneId, UUID destinationZoneId,
            Instant startedAt, Instant finishedAt, Long durationSeconds, BigDecimal preliminaryCost,
            String currency, BigDecimal unlockFee, BigDecimal pricePerMinute) {
        static RideResponse from(Ride r) {
            return new RideResponse(r.id, r.userId, r.vehicleId, r.reservationId, r.vehicleType,
                    r.status, r.originZoneId, r.destinationZoneId, r.startedAt, r.finishedAt,
                    r.durationSeconds, r.preliminaryCost, r.currency, r.unlockFee, r.pricePerMinute);
        }
    }
}
