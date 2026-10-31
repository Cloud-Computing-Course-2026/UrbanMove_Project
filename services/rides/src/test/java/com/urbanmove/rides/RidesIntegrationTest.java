package com.urbanmove.rides;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"rides.events.enabled=true", "rides.events.initial-delay-ms=3600000",
        "management.health.rabbit.enabled=false"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
@AutoConfigureObservability
class RidesIntegrationTest {
    static final UUID USER = UUID.fromString("30000000-0000-0000-0000-000000000001");
    static final UUID BIKE = UUID.fromString("10000000-0000-0000-0000-000000000001");
    static final UUID SCOOTER = UUID.fromString("10000000-0000-0000-0000-000000000002");
    static final UUID LOW_BATTERY = UUID.fromString("10000000-0000-0000-0000-000000000003");
    static final UUID RESERVATION = UUID.fromString("20000000-0000-0000-0000-000000000001");
    static final UUID ORIGIN = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID DESTINATION = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID FORBIDDEN = UUID.fromString("00000000-0000-0000-0000-000000000003");
    static final Instant NOW = Instant.parse("2026-10-29T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired RideService service;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired MutableClock clock;
    @Autowired OutboxPublisher publisher;
    @MockitoBean EventTransport transport;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv().getOrDefault("TEST_DB_URL",
                "jdbc:h2:mem:rides-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"));
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USER", "sa"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", ""));
    }

    @BeforeEach
    void clean() {
        reset(transport);
        clock.now.set(NOW);
        jdbc.update("DELETE FROM outbox_events");
        jdbc.update("DELETE FROM rides");
        jdbc.update("DELETE FROM demo_reservations");
        jdbc.update("UPDATE demo_vehicles SET ride_id = NULL, zone_id = ?, state = 'AVAILABLE' WHERE id = ?", ORIGIN, BIKE);
        jdbc.update("UPDATE demo_vehicles SET ride_id = NULL, state = 'RESERVED' WHERE id = ?", SCOOTER);
        jdbc.update("INSERT INTO demo_reservations VALUES (?, ?, ?, 'CONFIRMED', ?)",
                RESERVATION, USER, SCOOTER, Timestamp.from(NOW.plusSeconds(600)));
    }

    @Test
    void fullHttpLifecycleStoresEventsAndTariff() throws Exception {
        var result = mvc.perform(post("/rides").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "start-1").header("X-Correlation-ID", "flow-1")
                .content(json.writeValueAsString(new RideApi.StartRequest(USER, BIKE, null))))
                .andExpect(status().isCreated()).andExpect(header().string("X-Correlation-ID", "flow-1"))
                .andExpect(jsonPath("status").value("IN_PROGRESS"))
                .andExpect(jsonPath("originZoneId").value(ORIGIN.toString())).andReturn();
        String id = json.readTree(result.getResponse().getContentAsString()).get("id").asText();
        clock.now.set(NOW.plusSeconds(61));
        mvc.perform(post("/rides/{id}/finish", id).contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "finish-1").header("X-Correlation-ID", "flow-1")
                .content(json.writeValueAsString(new RideApi.FinishRequest(DESTINATION))))
                .andExpect(status().isOk()).andExpect(jsonPath("durationSeconds").value(61))
                .andExpect(jsonPath("preliminaryCost").value(0.70))
                .andExpect(jsonPath("status").value("FINISHED"));
        mvc.perform(get("/rides/{id}", id)).andExpect(status().isOk());
        assertThat(count("outbox_events")).isEqualTo(2);
        var payload = jdbc.queryForObject("SELECT payload FROM outbox_events WHERE event_type='RideFinished'", String.class);
        var event = json.readTree(payload);
        assertThat(event.path("correlationId").asText()).isEqualTo("flow-1");
        assertThat(event.path("source").asText()).isEqualTo("rides");
        assertThat(event.path("schemaVersion").asInt()).isEqualTo(1);
        assertThat(event.path("data").path("destinationZoneId").asText()).isEqualTo(DESTINATION.toString());
        assertThat(vehicleState(BIKE)).isEqualTo("AVAILABLE");
    }

    @Test
    void repeatedRequestsDoNotDuplicateRidesOrEvents() {
        var request = new RideApi.StartRequest(USER, BIKE, null);
        var first = service.start(request, "same-start", "flow");
        assertThat(service.start(request, "same-start", "retry").id()).isEqualTo(first.id());
        var finish = new RideApi.FinishRequest(DESTINATION);
        service.finish(first.id(), finish, "same-finish", "flow");
        service.finish(first.id(), finish, "same-finish", "retry");
        assertThat(count("rides")).isEqualTo(1);
        assertThat(count("outbox_events")).isEqualTo(2);
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(UUID.randomUUID(), BIKE, null),
                "same-start", "flow")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.finish(first.id(), new RideApi.FinishRequest(ORIGIN),
                "same-finish", "flow")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.finish(first.id(), finish, "other-finish", "flow"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void validatesReservationOwnershipAndConsumesOnlyOnce() {
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(UUID.randomUUID(), SCOOTER, RESERVATION),
                "wrong-user", "flow")).isInstanceOf(DomainException.class);
        assertThat(vehicleState(SCOOTER)).isEqualTo("RESERVED");
        var ride = service.start(new RideApi.StartRequest(USER, SCOOTER, RESERVATION), "reserved", "flow");
        assertThat(ride.vehicleType()).isEqualTo(Ride.VehicleType.SCOOTER);
        assertThat(jdbc.queryForObject("SELECT state FROM demo_reservations WHERE id = ?", String.class, RESERVATION))
                .isEqualTo("CONSUMED");
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(USER, SCOOTER, RESERVATION),
                "second-reservation-use", "flow")).isInstanceOf(DomainException.class);
    }

    @Test
    void expiredOrMissingReservationAndLowBatteryAreRejected() {
        jdbc.update("UPDATE demo_reservations SET expires_at = ?", Timestamp.from(NOW));
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(USER, SCOOTER, RESERVATION),
                "expired", "flow")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(USER, SCOOTER, null),
                "missing", "flow")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(USER, LOW_BATTERY, null),
                "low", "flow")).isInstanceOf(DomainException.class);
        assertThat(count("rides")).isZero();
        assertThat(count("outbox_events")).isZero();
    }

    @Test
    void invalidDestinationDoesNotChangeTripOrReleaseVehicle() {
        var ride = startBike();
        assertThatThrownBy(() -> service.finish(ride.id(), new RideApi.FinishRequest(FORBIDDEN), "bad", "flow"))
                .isInstanceOf(DomainException.class);
        assertThat(service.get(ride.id()).status()).isEqualTo(Ride.Status.IN_PROGRESS);
        assertThat(vehicleState(BIKE)).isEqualTo("IN_USE");
        assertThat(count("outbox_events")).isEqualTo(1);
    }

    @Test
    void userCannotHaveTwoActiveRidesAndFailedClaimRollsBack() {
        startBike();
        assertThatThrownBy(() -> service.start(new RideApi.StartRequest(USER, SCOOTER, RESERVATION), "second", "flow"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(vehicleState(SCOOTER)).isEqualTo("RESERVED");
        assertThat(jdbc.queryForObject("SELECT state FROM demo_reservations WHERE id = ?", String.class, RESERVATION))
                .isEqualTo("CONFIRMED");
        assertThat(count("outbox_events")).isEqualTo(1);
    }

    @Test
    void twoConcurrentStartsCanOnlyClaimVehicleOnce() throws Exception {
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a = pool.submit(() -> concurrentStart(gate, "concurrent-a"));
            var b = pool.submit(() -> concurrentStart(gate, "concurrent-b"));
            gate.countDown();
            assertThat(a.get(15, TimeUnit.SECONDS) + b.get(15, TimeUnit.SECONDS)).isEqualTo(1);
        }
        assertThat(count("rides")).isEqualTo(1);
        assertThat(count("outbox_events")).isEqualTo(1);
    }

    private int concurrentStart(CountDownLatch gate, String key) throws InterruptedException {
        gate.await();
        try {
            service.start(new RideApi.StartRequest(UUID.randomUUID(), BIKE, null), key, "concurrency");
            return 1;
        } catch (DomainException ex) {
            assertThat(ex.status.value()).isEqualTo(409);
            return 0;
        }
    }

    @Test
    void twoConcurrentFinishesEmitJustOneEvent() throws Exception {
        var ride = startBike();
        var gate = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a = pool.submit(() -> { gate.await(); return service.finish(ride.id(),
                    new RideApi.FinishRequest(DESTINATION), "same", "flow"); });
            var b = pool.submit(() -> { gate.await(); return service.finish(ride.id(),
                    new RideApi.FinishRequest(DESTINATION), "same", "flow"); });
            gate.countDown();
            assertThat(a.get(15, TimeUnit.SECONDS)).isEqualTo(b.get(15, TimeUnit.SECONDS));
        }
        assertThat(count("outbox_events")).isEqualTo(2);
    }

    @Test
    void startedMinutePricingHandlesSubsecondBoundary() {
        var ride = startBike();
        clock.now.set(NOW.plusSeconds(60).plusNanos(1000));
        var finished = service.finish(ride.id(), new RideApi.FinishRequest(DESTINATION), "end", "flow");
        assertThat(finished.preliminaryCost()).isEqualByComparingTo(new BigDecimal("0.70"));
    }

    @Test
    void immediateFinishBillsMinimumMinuteAndAllowsNewRide() {
        var ride = startBike();
        var finished = service.finish(ride.id(), new RideApi.FinishRequest(DESTINATION), "end", "flow");
        assertThat(finished.durationSeconds()).isZero();
        assertThat(finished.preliminaryCost()).isEqualByComparingTo("0.60");
        assertThat(service.start(new RideApi.StartRequest(USER, BIKE, null), "again", "flow").originZoneId())
                .isEqualTo(DESTINATION);
    }

    @Test
    void brokerFailureKeepsEventAndRetryUsesSameEventId() throws Exception {
        var ride = startBike();
        doThrow(new IllegalStateException("broker offline")).when(transport).publish(any(), anyString(), anyString());
        publisher.publishNext();
        assertThat(service.get(ride.id()).status()).isEqualTo(Ride.Status.IN_PROGRESS);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE published_at IS NULL AND attempts = 1", Long.class))
                .isEqualTo(1);
        reset(transport);
        clock.now.set(NOW.plusSeconds(2));
        publisher.publishNext();
        publisher.publishNext();
        verify(transport, times(1)).publish(any(), anyString(), anyString());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE published_at IS NOT NULL", Long.class))
                .isEqualTo(1);
    }

    @Test
    void malformedRequestsHaveConsistentErrors() throws Exception {
        mvc.perform(post("/rides").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("correlationId").exists());
        mvc.perform(post("/rides").header("Idempotency-Key", "x").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"not-a-uuid\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/rides").header("Idempotency-Key", " ").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new RideApi.StartRequest(USER, BIKE, null))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/rides/not-a-uuid")).andExpect(status().isBadRequest());
        mvc.perform(get("/rides/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void healthAndMetricsAreExposed() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    }

    private RideApi.RideResponse startBike() {
        return service.start(new RideApi.StartRequest(USER, BIKE, null), "start", "flow");
    }

    private long count(String table) {
        // Table names are constants in this test, never request input.
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    private String vehicleState(UUID id) {
        return jdbc.queryForObject("SELECT state FROM demo_vehicles WHERE id = ?", String.class, id);
    }

    @TestConfiguration
    static class TimeConfiguration {
        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock();
        }
    }

    static class MutableClock extends Clock {
        final AtomicReference<Instant> now = new AtomicReference<>(NOW);
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
        @Override public Instant instant() { return now.get(); }
    }
}
