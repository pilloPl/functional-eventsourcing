package io.pillopl.eventsource.shop.eventstore;


import io.pillopl.eventsource.shop.domain.ShopItem;
import io.pillopl.eventsource.shop.domain.ShopItemRepository;
import io.pillopl.eventsource.shop.domain.events.DomainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Component
public class EventSourcedShopItemRepository implements ShopItemRepository {

    private final EventStore eventStore;
    private final EventSerializer eventSerializer;

    @Autowired
    public EventSourcedShopItemRepository(EventStore eventStore, EventSerializer eventSerializer) {
        this.eventStore = eventStore;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public ShopItem save(ShopItem aggregate) {
        final List<DomainEvent> pendingEvents = aggregate.getUncommittedChanges();
        eventStore.saveEvents(
                aggregate.getUuid(),
                pendingEvents
                        .stream()
                        .map(eventSerializer::serialize)
                        .collect(toList()));
        return aggregate.markChangesAsCommitted();
    }

    @Override
    public ShopItem getByUUID(UUID uuid) {
        return ShopItem.rebuild(uuid, getRelatedEvents(uuid));
    }

    @Override
    public ShopItem getByUUIDat(UUID uuid, Instant at) {
        return ShopItem.
                rebuild(uuid,
                getRelatedEvents(uuid)
                        .stream()
                        .filter(evt -> !evt.when().isAfter(at))
                        .collect(toList()));
    }


    private List<DomainEvent> getRelatedEvents(UUID uuid) {
        return eventStore.getEventsForAggregate(uuid)
                .stream()
                .map(eventSerializer::deserialize)
                .collect(toList());
    }

}
