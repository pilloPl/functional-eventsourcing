package io.pillopl.eventsource.shop.eventstore;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "event_descriptors")
public class EventDescriptor {

    public enum Status {
        PENDING, SENT
    }

    @Id
    @GeneratedValue(generator = "event_descriptors_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "event_descriptors_seq", sequenceName = "event_descriptors_seq", allocationSize = 1)
    @Getter
    private Long id;

    @Getter
    @Column(nullable = false, length = 600)
    private String body;

    @Getter
    @Column(nullable = false, name = "occurred_at")
    private Instant occurredAt;

    @Getter
    @Column(nullable = false, length = 60)
    private String type;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    private Status status = Status.PENDING;

    @Getter
    @Column(nullable = false, name = "aggregate_uuid", length = 36)
    private UUID aggregateUUID;

    EventDescriptor(String body, Instant occurredAt, String type, UUID aggregateUUID) {
        this.body = body;
        this.occurredAt = occurredAt;
        this.type = type;
        this.aggregateUUID = aggregateUUID;
    }

    private EventDescriptor() {
    }

    public EventDescriptor sent() {
        this.status = Status.SENT;
        return this;
    }
}
