package com.urbanmove.rides.messaging;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rides.events.enabled", havingValue = "true")
public class RabbitEvents {
    public static final String EXCHANGE = "urbanmove.events";
    public static final String QUEUE = "urbanmove.rides.events";

    @Bean
    TopicExchange ridesExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue ridesEventQueue() {
        // Demo inspection queue. History and analytics must each declare their own queue and bindings.
        return new Queue(QUEUE, true);
    }

    @Bean
    Binding startedBinding(Queue ridesEventQueue, TopicExchange ridesExchange) {
        return BindingBuilder.bind(ridesEventQueue).to(ridesExchange).with("RideStarted");
    }

    @Bean
    Binding finishedBinding(Queue ridesEventQueue, TopicExchange ridesExchange) {
        return BindingBuilder.bind(ridesEventQueue).to(ridesExchange).with("RideFinished");
    }

    @Bean
    EventTransport rabbitTransport(RabbitTemplate template) {
        template.setMandatory(true);
        template.setReturnsCallback(returned -> { /* CorrelationData carries the returned message. */ });
        return (UUID id, String type, String payload) -> {
            var properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setContentEncoding(StandardCharsets.UTF_8.name());
            properties.setMessageId(id.toString());
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            var correlation = new CorrelationData(UUID.randomUUID().toString());
            template.send(EXCHANGE, type, new Message(payload.getBytes(StandardCharsets.UTF_8), properties), correlation);
            var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.isAck() || correlation.getReturned() != null) {
                throw new IllegalStateException("Broker no confirmo el evento o no hay cola de destino");
            }
        };
    }
}
