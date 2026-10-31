package com.urbanmove.rides.messaging.outbox;

import com.urbanmove.rides.messaging.EventTransport;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "rides.events.enabled", havingValue = "true")
public class OutboxPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(OutboxPublisher.class);
    private final JdbcTemplate jdbc;
    private final EventTransport transport;
    private final Clock clock;
    private final MeterRegistry metrics;

    public OutboxPublisher(JdbcTemplate jdbc, EventTransport transport, Clock clock, MeterRegistry metrics) {
        this.jdbc = jdbc;
        this.transport = transport;
        this.clock = clock;
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${rides.events.poll-ms:1000}", initialDelayString = "${rides.events.initial-delay-ms:1000}")
    @Transactional
    public void publishNext() {
        // One locked row per transaction serializes competing publishers. No in-memory ownership.
        var pending = jdbc.query("""
                SELECT event_id, event_type, payload, attempts FROM outbox_events
                WHERE published_at IS NULL AND next_attempt_at <= ? ORDER BY sequence_id LIMIT 1 FOR UPDATE
                """, (rs, row) -> new Pending(rs.getObject(1, UUID.class), rs.getString(2),
                        rs.getString(3), rs.getInt(4)), Timestamp.from(clock.instant()));
        if (pending.isEmpty()) {
            return;
        }
        var event = pending.getFirst();
        try {
            transport.publish(event.id(), event.type(), event.payload());
            jdbc.update("UPDATE outbox_events SET published_at = ?, attempts = attempts + 1, last_error = NULL WHERE event_id = ?",
                    Timestamp.from(clock.instant()), event.id());
            metrics.counter("rides.events", "result", "published").increment();
            LOG.atInfo().addKeyValue("eventId", event.id()).addKeyValue("type", event.type()).log("event_published");
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            long delay = Math.min(60, 1L << Math.min(event.attempts(), 6));
            jdbc.update("""
                    UPDATE outbox_events SET attempts = attempts + 1, next_attempt_at = ?, last_error = ?
                    WHERE event_id = ?
                    """, Timestamp.from(clock.instant().plusSeconds(delay)), ex.getClass().getSimpleName(), event.id());
            metrics.counter("rides.events", "result", "failed").increment();
            LOG.atWarn().addKeyValue("eventId", event.id()).addKeyValue("retrySeconds", delay)
                    .addKeyValue("error", ex.getClass().getSimpleName()).log("event_pending_retry");
        }
    }

    private record Pending(UUID id, String type, String payload, int attempts) { }
}
