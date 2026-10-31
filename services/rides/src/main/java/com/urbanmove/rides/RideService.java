package com.urbanmove.rides;

import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideService {
    private final RideRepository rides;
    private final MobilityGateway mobility;
    private final Outbox outbox;
    private final Clock clock;
    private final MeterRegistry metrics;
    private final BigDecimal unlockFee;
    private final BigDecimal bicycleRate;
    private final BigDecimal scooterRate;

    public RideService(RideRepository rides, MobilityGateway mobility, Outbox outbox, Clock clock,
            MeterRegistry metrics, @Value("${rides.pricing.unlock-fee:0.50}") BigDecimal unlockFee,
            @Value("${rides.pricing.bicycle-per-minute:0.10}") BigDecimal bicycleRate,
            @Value("${rides.pricing.scooter-per-minute:0.20}") BigDecimal scooterRate) {
        this.rides = rides;
        this.mobility = mobility;
        this.outbox = outbox;
        this.clock = clock;
        this.metrics = metrics;
        this.unlockFee = validPrice(unlockFee);
        this.bicycleRate = validPrice(bicycleRate);
        this.scooterRate = validPrice(scooterRate);
    }

    private static BigDecimal validPrice(BigDecimal value) {
        if (value.signum() < 0 || value.precision() > 10 || value.scale() > 2) {
            throw new IllegalArgumentException("Tarifa invalida: maximo dos decimales, no negativa");
        }
        return value.setScale(2);
    }

    @Transactional
    public RideApi.RideResponse start(RideApi.StartRequest request, String key, String correlationId) {
        var previous = rides.findByStartKey(key);
        if (previous.isPresent()) {
            Ride r = previous.get();
            if (!r.userId.equals(request.userId()) || !r.vehicleId.equals(request.vehicleId())
                    || !Objects.equals(r.reservationId, request.reservationId())) {
                throw DomainException.conflict("Idempotency-Key ya utilizada con otros datos");
            }
            return RideApi.RideResponse.from(r);
        }
        Ride r = new Ride();
        r.id = UUID.randomUUID();
        r.userId = request.userId();
        r.vehicleId = request.vehicleId();
        r.reservationId = request.reservationId();
        r.activeVehicleId = r.vehicleId;
        r.activeUserId = r.userId;
        r.startedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);
        var vehicle = mobility.claim(r.id, r.vehicleId, r.userId, r.reservationId, r.startedAt);
        r.originZoneId = vehicle.originZoneId();
        r.vehicleType = vehicle.type();
        r.status = Ride.Status.IN_PROGRESS;
        r.startKey = key;
        r.unlockFee = unlockFee;
        r.pricePerMinute = r.vehicleType == Ride.VehicleType.BICYCLE ? bicycleRate : scooterRate;
        r.currency = "EUR";
        rides.saveAndFlush(r);
        outbox.append("RideStarted", r, correlationId);
        metrics.counter("rides.operations", "operation", "start").increment();
        return RideApi.RideResponse.from(r);
    }

    @Transactional
    public RideApi.RideResponse finish(UUID id, RideApi.FinishRequest request, String key, String correlationId) {
        Ride r = rides.findForUpdate(id).orElseThrow(() -> DomainException.notFound("Viaje inexistente"));
        if (r.status == Ride.Status.FINISHED) {
            if (key.equals(r.finishKey) && request.destinationZoneId().equals(r.destinationZoneId)) {
                return RideApi.RideResponse.from(r);
            }
            throw DomainException.conflict("El viaje ya ha finalizado");
        }
        var end = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (end.isBefore(r.startedAt)) {
            throw DomainException.conflict("El reloj del sistema es anterior al inicio del viaje");
        }
        mobility.finish(r.id, r.vehicleId, request.destinationZoneId());
        r.finishedAt = end;
        r.durationSeconds = Duration.between(r.startedAt, end).getSeconds();
        // Bill started minutes without rounding away fractional seconds, minimum one minute.
        var elapsed = Duration.between(r.startedAt, end);
        long seconds = elapsed.getSeconds();
        long minutes = Math.max(1, seconds / 60 + (seconds % 60 > 0 || elapsed.getNano() > 0 ? 1 : 0));
        r.preliminaryCost = r.unlockFee.add(r.pricePerMinute.multiply(BigDecimal.valueOf(minutes)))
                .setScale(2, RoundingMode.HALF_UP);
        r.destinationZoneId = request.destinationZoneId();
        r.finishKey = key;
        r.status = Ride.Status.FINISHED;
        r.activeUserId = null;
        r.activeVehicleId = null;
        rides.saveAndFlush(r);
        outbox.append("RideFinished", r, correlationId);
        metrics.counter("rides.operations", "operation", "finish").increment();
        return RideApi.RideResponse.from(r);
    }

    @Transactional(readOnly = true)
    public RideApi.RideResponse get(UUID id) {
        return RideApi.RideResponse.from(rides.findById(id)
                .orElseThrow(() -> DomainException.notFound("Viaje inexistente")));
    }
}
