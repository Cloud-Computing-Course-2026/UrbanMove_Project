package com.urbanmove.rides.messaging;

import java.util.UUID;

public interface EventTransport {
    /** Return only after the broker confirms durable acceptance; throw on failure or an unroutable event. */
    void publish(UUID eventId, String type, String payload) throws Exception;
}
