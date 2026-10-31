package com.urbanmove.rides;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RideMetrics {
    public RideMetrics(MeterRegistry registry, JdbcTemplate jdbc) {
        Gauge.builder("rides.active", jdbc,
                db -> db.queryForObject("SELECT COUNT(*) FROM rides WHERE status = 'IN_PROGRESS'", Long.class))
                .register(registry);
        Gauge.builder("rides.outbox.pending", jdbc,
                db -> db.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE published_at IS NULL", Long.class))
                .register(registry);
    }
}
