package com.urbanmove.rides.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rides")
public class Ride {
    public enum Status { IN_PROGRESS, FINISHED }
    public enum VehicleType { BICYCLE, SCOOTER }

    @Id
    private UUID id;
    private UUID userId;
    private UUID vehicleId;
    private UUID reservationId;
    private UUID originZoneId;
    private UUID destinationZoneId;
    private UUID activeVehicleId;
    private UUID activeUserId;
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    @Enumerated(EnumType.STRING)
    private Status status;
    private Instant startedAt;
    private Instant finishedAt;
    private Long durationSeconds;
    private BigDecimal unlockFee;
    private BigDecimal pricePerMinute;
    private BigDecimal preliminaryCost;
    private String currency;
    private String startKey;
    private String finishKey;

    protected Ride() { }

    public static Ride start(UUID id, UUID userId, UUID vehicleId, UUID reservationId,
            Instant startedAt, UUID originZoneId, VehicleType vehicleType, String startKey,
            BigDecimal unlockFee, BigDecimal pricePerMinute) {
        Ride ride = new Ride();
        ride.id = id;
        ride.userId = userId;
        ride.vehicleId = vehicleId;
        ride.reservationId = reservationId;
        ride.activeVehicleId = vehicleId;
        ride.activeUserId = userId;
        ride.startedAt = startedAt;
        ride.originZoneId = originZoneId;
        ride.vehicleType = vehicleType;
        ride.status = Status.IN_PROGRESS;
        ride.startKey = startKey;
        ride.unlockFee = unlockFee;
        ride.pricePerMinute = pricePerMinute;
        ride.currency = "EUR";
        return ride;
    }

    public void finish(Instant finishedAt, long durationSeconds, BigDecimal preliminaryCost,
            UUID destinationZoneId, String finishKey) {
        this.finishedAt = finishedAt;
        this.durationSeconds = durationSeconds;
        this.preliminaryCost = preliminaryCost;
        this.destinationZoneId = destinationZoneId;
        this.finishKey = finishKey;
        this.status = Status.FINISHED;
        this.activeUserId = null;
        this.activeVehicleId = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getOriginZoneId() {
        return originZoneId;
    }

    public UUID getDestinationZoneId() {
        return destinationZoneId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public BigDecimal getUnlockFee() {
        return unlockFee;
    }

    public BigDecimal getPricePerMinute() {
        return pricePerMinute;
    }

    public BigDecimal getPreliminaryCost() {
        return preliminaryCost;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStartKey() {
        return startKey;
    }

    public String getFinishKey() {
        return finishKey;
    }
}
