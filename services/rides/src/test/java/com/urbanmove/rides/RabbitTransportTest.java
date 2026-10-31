package com.urbanmove.rides;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "RUN_RABBIT_TESTS", matches = "true")
@ActiveProfiles("demo")
@SpringBootTest(properties = {"rides.events.enabled=true", "rides.events.initial-delay-ms=3600000",
        "spring.datasource.url=jdbc:h2:mem:rabbit-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password="})
class RabbitTransportTest {
    @Autowired AmqpAdmin admin;
    @Autowired RabbitTemplate rabbit;
    @Autowired RideService rides;
    @Autowired OutboxPublisher publisher;
    @Autowired EventTransport transport;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;

    @Test
    void lifecycleReachesDurableQueueAndUnroutableMessagesFail() throws Exception {
        admin.purgeQueue(RabbitEvents.QUEUE);
        var ride = rides.start(new RideApi.StartRequest(RidesIntegrationTest.USER,
                RidesIntegrationTest.BIKE, null), "broker-start", "broker-flow");
        rides.finish(ride.id(), new RideApi.FinishRequest(RidesIntegrationTest.DESTINATION), "broker-finish", "broker-flow");
        publisher.publishNext();
        publisher.publishNext();
        var started = rabbit.receive(RabbitEvents.QUEUE, 10000);
        var finished = rabbit.receive(RabbitEvents.QUEUE, 10000);
        assertThat(started).isNotNull();
        assertThat(finished).isNotNull();
        var first = json.readTree(new String(started.getBody(), StandardCharsets.UTF_8));
        var second = json.readTree(new String(finished.getBody(), StandardCharsets.UTF_8));
        assertThat(first.path("type").asText()).isEqualTo("RideStarted");
        assertThat(second.path("type").asText()).isEqualTo("RideFinished");
        assertThat(second.path("data").path("id").asText()).isEqualTo(ride.id().toString());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE published_at IS NOT NULL", Long.class))
                .isEqualTo(2);
        assertThatThrownBy(() -> transport.publish(UUID.randomUUID(), "NoMatchingBinding", "{}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
