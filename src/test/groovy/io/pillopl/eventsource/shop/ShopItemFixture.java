package io.pillopl.eventsource.shop;


import com.google.common.collect.ImmutableList;
import io.pillopl.eventsource.shop.domain.ShopItem;
import io.pillopl.eventsource.shop.domain.ShopItemStatus;
import io.pillopl.eventsource.shop.domain.commands.MarkPaymentTimeout;
import io.pillopl.eventsource.shop.domain.commands.OrderWithTimeout;
import io.pillopl.eventsource.shop.domain.commands.Pay;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static java.time.Instant.now;

public class ShopItemFixture {

    public static final Instant ANY_TIME = now();
    public static final int ANY_NUMBER_OF_HOURS_TO_PAYMENT_TIMEOUT = 48;
    public static final BigDecimal ANY_PRICE = BigDecimal.TEN;


    public static ShopItem initialized() {
        return new ShopItem(null, ImmutableList.of(), ShopItemStatus.INITIALIZED);
    }

    public static ShopItem ordered(UUID uuid) {
        return initialized()
                .order(new OrderWithTimeout(uuid, ANY_PRICE, ANY_TIME, ANY_NUMBER_OF_HOURS_TO_PAYMENT_TIMEOUT)).get()
                .markChangesAsCommitted();
    }

    public static ShopItem paid(UUID uuid) {
        return initialized()
                .order(new OrderWithTimeout(uuid, ANY_PRICE, now(), ANY_NUMBER_OF_HOURS_TO_PAYMENT_TIMEOUT)).get()
                .pay(new Pay(uuid, now())).get()
                .markChangesAsCommitted();
    }

    public static ShopItem withTimeout(UUID uuid) {
        return initialized()
                .order(new OrderWithTimeout(uuid, ANY_PRICE, now(), ANY_NUMBER_OF_HOURS_TO_PAYMENT_TIMEOUT)).get()
                .markTimeout(new MarkPaymentTimeout(uuid, now())).get()
                .markChangesAsCommitted();
    }

    public static ShopItem withTimeoutAndPaid(UUID uuid) {
        return initialized()
                .order(new OrderWithTimeout(uuid, ANY_PRICE, now(), ANY_NUMBER_OF_HOURS_TO_PAYMENT_TIMEOUT)).get()
                .markTimeout(new MarkPaymentTimeout(uuid, now())).get()
                .pay(new Pay(uuid, now())).get()
                .markChangesAsCommitted();
    }

}
