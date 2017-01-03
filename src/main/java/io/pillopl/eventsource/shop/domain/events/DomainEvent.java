package io.pillopl.eventsource.shop.domain.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = ItemOrdered.TYPE, value = ItemOrdered.class),
        @JsonSubTypes.Type(name = ItemPaymentTimeout.TYPE, value = ItemPaymentTimeout.class),
        @JsonSubTypes.Type(name = ItemPaid.TYPE, value = ItemPaid.class)
})
public interface DomainEvent {

    String type();
    Instant when();
    UUID aggregateUuid();
}

