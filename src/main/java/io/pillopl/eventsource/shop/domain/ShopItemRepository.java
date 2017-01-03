package io.pillopl.eventsource.shop.domain;

import java.time.Instant;
import java.util.UUID;

public interface ShopItemRepository {

    ShopItem save(ShopItem aggregate) ;

    ShopItem getByUUID(UUID uuid);

    ShopItem getByUUIDat(UUID uuid, Instant at);

}
