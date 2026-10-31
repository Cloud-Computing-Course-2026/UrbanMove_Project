package com.urbanmove.rides.dto;

import com.urbanmove.rides.model.Ride;
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
        public static RideResponse from(Ride r) {
            return new RideResponse(r.getId(), r.getUserId(), r.getVehicleId(), r.getReservationId(), r.getVehicleType(),
                    r.getStatus(), r.getOriginZoneId(), r.getDestinationZoneId(), r.getStartedAt(), r.getFinishedAt(),
                    r.getDurationSeconds(), r.getPreliminaryCost(), r.getCurrency(), r.getUnlockFee(), r.getPricePerMinute());
        }
    }
}
