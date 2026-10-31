package com.urbanmove.rides.messaging.outbox;

import com.urbanmove.rides.dto.RideApi;
import com.urbanmove.rides.model.Ride;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Outbox {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final Clock clock;

    public Outbox(JdbcTemplate jdbc, ObjectMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.clock = clock;
    }

    public record DomainEvent(UUID eventId, Instant timestamp, String source, String type,
            int schemaVersion, String correlationId, RideApi.RideResponse data) { }

    public void append(String type, Ride ride, String correlationId) {
        var event = new DomainEvent(UUID.randomUUID(), clock.instant(), "rides", type,
                1, correlationId, RideApi.RideResponse.from(ride));
        try {
            jdbc.update("""
                    INSERT INTO outbox_events (event_id, event_type, payload, created_at, next_attempt_at, attempts)
                    VALUES (?, ?, ?, ?, ?, 0)
                    """, event.eventId(), type, json.writeValueAsString(event),
                    Timestamp.from(event.timestamp()), Timestamp.from(event.timestamp()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el evento; se revierte el viaje", ex);
        }
    }
}
