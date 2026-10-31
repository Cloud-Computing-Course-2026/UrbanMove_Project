package com.urbanmove.rides;

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
    UUID id;
    UUID userId;
    UUID vehicleId;
    UUID reservationId;
    UUID originZoneId;
    UUID destinationZoneId;
    UUID activeVehicleId;
    UUID activeUserId;
    @Enumerated(EnumType.STRING)
    VehicleType vehicleType;
    @Enumerated(EnumType.STRING)
    Status status;
    Instant startedAt;
    Instant finishedAt;
    Long durationSeconds;
    BigDecimal unlockFee;
    BigDecimal pricePerMinute;
    BigDecimal preliminaryCost;
    String currency;
    String startKey;
    String finishKey;

    protected Ride() { }
}
