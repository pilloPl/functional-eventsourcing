package io.pillopl.eventsource.shop.eventstore.publisher;

import io.pillopl.eventsource.shop.eventstore.EventDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

    private final PendingEventFetcher pendingEventFetcher;
    private final PublishChannel publishChannel;

    @Autowired
    public EventPublisher(PendingEventFetcher pendingEventFetcher, PublishChannel publishChannel) {
        this.pendingEventFetcher = pendingEventFetcher;
        this.publishChannel = publishChannel;
    }

    @Scheduled(fixedRate = 2000)
    public void publishPending() {
        pendingEventFetcher.listPending().forEach(this::sendSafely);
    }

    private EventDescriptor sendSafely(EventDescriptor event) {
        final String body = event.getBody();
        try {
            log.info("about to send: {}", body);
            publishChannel.send(body, event.getAggregateUUID());
            pendingEventFetcher.save(event.sent());
            log.info("send: {}", body);
        } catch (Exception e) {
            log.error("cannot send {}", body, e);
        }
        return event;
    }

}

