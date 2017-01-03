package io.pillopl.eventsource.shop.domain;

import com.google.common.collect.ImmutableList;
import io.pillopl.eventsource.shop.domain.commands.MarkPaymentTimeout;
import io.pillopl.eventsource.shop.domain.commands.OrderWithTimeout;
import io.pillopl.eventsource.shop.domain.commands.Pay;
import io.pillopl.eventsource.shop.domain.events.DomainEvent;
import io.pillopl.eventsource.shop.domain.events.ItemOrdered;
import io.pillopl.eventsource.shop.domain.events.ItemPaid;
import io.pillopl.eventsource.shop.domain.events.ItemPaymentTimeout;
import javaslang.API;
import javaslang.Function1;
import javaslang.Function2;
import javaslang.control.Try;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


import static io.pillopl.eventsource.shop.domain.ShopItemStatus.INITIALIZED;
import static io.pillopl.eventsource.shop.domain.ShopItemStatus.ORDERED;
import static io.pillopl.eventsource.shop.domain.ShopItemStatus.PAID;
import static io.pillopl.eventsource.shop.domain.ShopItemStatus.PAYMENT_MISSING;
import static javaslang.API.Case;
import static javaslang.Predicates.instanceOf;
import static javaslang.collection.List.ofAll;
import static javaslang.control.Try.*;

@RequiredArgsConstructor
@Getter
@Wither
public class ShopItem {

    /**
     * State transitions -> MUST accept the facts and CANNOT contain any behaviour! (Event handlers).
     * f(state, event) -> state
     */
    private static final Function2<ShopItem, ItemOrdered, ShopItem> ordered =
            (state, event) -> state
                    .withUuid(event.getUuid())
                    .withStatus(ORDERED);

    private static final Function2<ShopItem, ItemPaid, ShopItem> paid =
            (state, event) -> state
                    .withUuid(event.getUuid())
                    .withStatus(PAID);

    private static final Function2<ShopItem, ItemPaymentTimeout, ShopItem> timedOut =
            (state, event) -> state
                    .withUuid(event.getUuid())
                    .withStatus(PAYMENT_MISSING);

    private static final Function2<ShopItem, DomainEvent, ShopItem> appendChange =
            (state, event) -> state
                    .patternMatch(event)
                    .withChanges(ImmutableList
                            .<DomainEvent>builder()
                            .addAll(state.changes)
                            .add(event)
                            .build());


    /**
     * Behaviour transitions -> Can fail or return new events
     * f(state, command) -> events
     */
    private static final Function2<ShopItem, Pay, DomainEvent> pay =
            (state, command) ->
                    new ItemPaid(state.uuid, command.getWhen());

    private static final Function2<ShopItem, OrderWithTimeout, DomainEvent> order =
            (state, command) ->
                    new ItemOrdered(
                            command.getUuid(),
                            command.getWhen(),
                            state.calculatePaymentTimeoutDate(command.getWhen(), command.getHoursToPaymentTimeout()),
                            command.getPrice());

    private static final Function2<ShopItem, MarkPaymentTimeout, DomainEvent> markTimeout =
            (state, command) -> new ItemPaymentTimeout(state.uuid, command.getWhen());

    private static final Function1<ShopItem, ShopItem> noOp =
            (state) -> state;


    /**
     * low level details - state
     */
    private final UUID uuid;
    private final ImmutableList<DomainEvent> changes;
    private final ShopItemStatus status;



    /**
     * Command Handlers
     */
    public Try<ShopItem> order(OrderWithTimeout command) {
        return of(() -> {
            if (status == INITIALIZED) {
                return appendChange.apply(this, order.apply(this, command));
            } else {
                return noOp.apply(this);
            }
        });
    }

    public Try<ShopItem> pay(Pay command) {
        return of(() -> {
            throwIfStateIs(INITIALIZED, "Cannot pay for not ordered item");
            if (status != PAID) {
                return appendChange.apply(this, pay.apply(this, command));
            } else {
                return noOp.apply(this);
            }
        });
    }

    public Try<ShopItem> markTimeout(MarkPaymentTimeout command) {
        return of(() -> {
            throwIfStateIs(INITIALIZED, "Payment is not missing yet");
            throwIfStateIs(PAID, "Item already paid");
            if (status == ORDERED) {
                return appendChange.apply(this, markTimeout.apply(this, command));
            } else {
                return noOp.apply(this);
            }
        });

    }

    private void throwIfStateIs(ShopItemStatus unexpectedState, String msg) {
        if (status == unexpectedState) {
            throw new IllegalStateException(msg + (" UUID: " + uuid));
        }
    }

    private Instant calculatePaymentTimeoutDate(Instant boughtAt, int hoursToPaymentTimeout) {
        final Instant paymentTimeout = boughtAt.plus(hoursToPaymentTimeout, ChronoUnit.MINUTES);
        if (paymentTimeout.isBefore(boughtAt)) {
            throw new IllegalArgumentException("Payment timeout day is before ordering date!");
        }
        return paymentTimeout;
    }


    /**
     * Rebuilding aggregate with left fold and pattern match
     */
    public static ShopItem rebuild(UUID uuid, List<DomainEvent> history) {
        return ofAll(history)
                .foldLeft(
                        initialState(uuid),
                        ShopItem::patternMatch);

    }

    private static ShopItem initialState(UUID uuid) {
        return new ShopItem(uuid, ImmutableList.of(), INITIALIZED);
    }


    private ShopItem patternMatch(DomainEvent event) {
        return API.Match(event).of(
                Case(instanceOf(ItemPaid.class), this::paid),
                Case(instanceOf(ItemOrdered.class), this::ordered),
                Case(instanceOf(ItemPaymentTimeout.class), this::paymentMissed)
        );
    }


    /**
     * Event handlers - must accept the fact
     */
    private ShopItem paid(ItemPaid event) {
        return paid.apply(this, event);
    }

    private ShopItem ordered(ItemOrdered event) {
        return ordered.apply(this, event);
    }

    private ShopItem paymentMissed(ItemPaymentTimeout event) {
        return timedOut.apply(this, event);
    }

    /**
     * Getting and clearing all the changes (finishing the work with unit of work - aggregate)
     */

    public ImmutableList<DomainEvent> getUncommittedChanges() {
        return changes;
    }

    public ShopItem markChangesAsCommitted() {
        return this.withChanges(ImmutableList.of());
    }
    
}
