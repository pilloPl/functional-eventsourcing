package io.pillopl.eventsource.shop;


import io.pillopl.eventsource.shop.domain.commands.Order;
import io.pillopl.eventsource.shop.domain.commands.OrderWithTimeout;
import io.pillopl.eventsource.shop.domain.commands.MarkPaymentTimeout;
import io.pillopl.eventsource.shop.domain.commands.Pay;

import java.time.Instant;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;

public class CommandFixture {

    public static Order orderItemCommand(UUID uuid) {
        return new Order(uuid, ZERO, now());
    }

    public static Order orderItemCommand(UUID uuid, Instant when) {
        return new Order(uuid, ZERO, when);
    }

    public static Pay payItemCommand(UUID uuid) {
        return new Pay(uuid, now());
    }

    public static Pay payItemCommand(UUID uuid, Instant when) {
        return new Pay(uuid, when);
    }

    public static MarkPaymentTimeout markPaymentTimeoutCommand(UUID uuid) {
        return new MarkPaymentTimeout(uuid, now());
    }

    public static MarkPaymentTimeout markPaymentTimeoutCommand(UUID uuid, Instant when) {
        return new MarkPaymentTimeout(uuid, when);
    }
}
