package io.pillopl.eventsource.shop.eventstore.publisher;

import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.Publisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PublishChannel {

    @Publisher(channel = Source.OUTPUT)
    public String send(String payload, @Header UUID uuid) {
        return payload;
    }
}

