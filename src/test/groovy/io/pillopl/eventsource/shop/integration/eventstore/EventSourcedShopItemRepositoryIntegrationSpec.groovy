package io.pillopl.eventsource.shop.integration.eventstore

import io.pillopl.eventsource.shop.domain.ShopItem
import io.pillopl.eventsource.shop.domain.commands.OrderWithTimeout
import io.pillopl.eventsource.shop.domain.commands.Pay
import io.pillopl.eventsource.shop.eventstore.EventSourcedShopItemRepository
import io.pillopl.eventsource.shop.integration.IntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Subject

import java.time.Instant

import static io.pillopl.eventsource.shop.ShopItemFixture.initialized
import static io.pillopl.eventsource.shop.domain.ShopItemStatus.ORDERED
import static io.pillopl.eventsource.shop.domain.ShopItemStatus.PAID
import static java.time.LocalDate.now
import static java.time.ZoneId.systemDefault
import static java.time.temporal.ChronoUnit.DAYS

class EventSourcedShopItemRepositoryIntegrationSpec extends IntegrationSpec {

    private static final UUID uuid = UUID.randomUUID()
    private static final int PAYMENT_DEADLINE_IN_HOURS = 48
    private static final Instant TODAY = now().atStartOfDay(systemDefault()).toInstant()
    private static final Instant TOMORROW = TODAY.plus(1, DAYS)
    private static final Instant DAY_AFTER_TOMORROW =  TOMORROW.plus(1, DAYS)
    private static final BigDecimal ANY_PRICE = BigDecimal.TEN


    @Subject
    @Autowired
    EventSourcedShopItemRepository shopItemRepository

    def 'should store and load item'() {
        given:
            ShopItem stored =
                    initialized()
                            .order(new OrderWithTimeout(uuid, ANY_PRICE, TODAY, PAYMENT_DEADLINE_IN_HOURS)).get()
        when:
            shopItemRepository.save(stored)
        and:
            ShopItem loaded = shopItemRepository.getByUUID(uuid)
        then:
            loaded.uuid == stored.uuid
            loaded.status == stored.status
    }

    def 'should reconstruct item at given moment'() {
        given:
            ShopItem stored = initialized()
                    .order(new OrderWithTimeout(uuid, ANY_PRICE, TOMORROW, PAYMENT_DEADLINE_IN_HOURS))
                    .get()
                    .pay(new Pay(uuid, DAY_AFTER_TOMORROW))
                    .get()
        when:
            shopItemRepository.save(stored)
        and:
            ShopItem bought = shopItemRepository.getByUUIDat(uuid, TOMORROW)
            ShopItem paid = shopItemRepository.getByUUIDat(uuid, DAY_AFTER_TOMORROW)

        then:
            bought.status == ORDERED
            paid.status == PAID

    }

}
