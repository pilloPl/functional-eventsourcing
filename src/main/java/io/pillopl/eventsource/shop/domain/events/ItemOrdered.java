package io.pillopl.eventsource.shop.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemOrdered implements DomainEvent {

    public static final String TYPE = "item.ordered";

    private UUID uuid;
    private Instant when;
    private Instant paymentTimeoutDate;
    private BigDecimal price;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Instant when() {
        return when;
    }

    @Override
    public UUID aggregateUuid() {
        return uuid;
    }
}
