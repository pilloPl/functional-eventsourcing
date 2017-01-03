package io.pillopl.eventsource.shop.integration

import io.pillopl.eventsource.shop.domain.events.ItemOrdered
import io.pillopl.eventsource.shop.domain.events.ItemPaid
import io.pillopl.eventsource.shop.domain.events.ItemPaymentTimeout
import io.pillopl.eventsource.shop.eventstore.publisher.EventPublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.cloud.stream.messaging.Source
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage
import spock.util.concurrent.PollingConditions

import java.util.concurrent.BlockingQueue

import static java.util.UUID.randomUUID

class E2ESpec extends IntegrationSpec {

    private ANY_UUID = randomUUID()

    @Autowired Source source
    @Autowired Sink commands
    @Autowired MessageCollector eventsCollector
    @Autowired EventPublisher eventPublisher

    BlockingQueue<Message<?>> events
    PollingConditions conditions = new PollingConditions(timeout: 12, initialDelay: 0, factor: 1)

    def setup() {
        events = eventsCollector.forChannel(source.output())
    }

    def 'received order command should result in emitted item ordered event'() {
        when:
            commands.input().send(new GenericMessage<>(sampleOrderInJson(ANY_UUID)))

        then:
            conditions.eventually {
                expectedMessageThatContains(ItemOrdered.TYPE)
            }
    }

    def 'received pay command should result in emitted item paid event'() {
        when:
            commands.input().send(new GenericMessage<>(sampleOrderInJson(ANY_UUID)))
        and:
            commands.input().send(new GenericMessage<>(samplePayInJson(ANY_UUID)))

        then:
            conditions.eventually {
                expectedMessageThatContains(ItemOrdered.TYPE)
            }
        and:
            conditions.eventually {
                expectedMessageThatContains(ItemPaid.TYPE)
            }
    }

    def 'received mark missing payment command should result in emitted marked as missed event'() {
        when:
            commands.input().send(new GenericMessage<>(sampleOrderInJson(ANY_UUID)))
        and:
            commands.input().send(new GenericMessage<>(sampleMarkTimeoutInJson(ANY_UUID)))

        then:
            conditions.eventually {
                expectedMessageThatContains(ItemOrdered.TYPE)
            }
        and:
            conditions.eventually {
                expectedMessageThatContains(ItemPaymentTimeout.TYPE)
            }
    }

    private static String sampleOrderInJson(UUID uuid) {
        return "{\"type\":\"item.order\",\"uuid\":\"$uuid\",\"when\":\"2016-10-06T10:28:23.956Z\"}"
    }

    private static String samplePayInJson(UUID uuid) {
        return "{\"type\":\"item.pay\",\"uuid\":\"$uuid\",\"when\":\"2016-10-06T10:29:23.956Z\"}"
    }

    private static String sampleMarkTimeoutInJson(UUID uuid) {
        return "{\"type\":\"item.markPaymentTimeout\",\"uuid\":\"$uuid\",\"when\":\"2016-10-06T10:29:23.956Z\"}"
    }

    void expectedMessageThatContains(String text) {
        Message<String> msg = events.poll()
        println msg
        assert msg != null && msg.getPayload().contains(text)
        println "GOT IT: contains " + msg.getPayload().contains(text)
        assert msg.getPayload().contains(text)
    }

}
