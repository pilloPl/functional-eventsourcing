package io.pillopl.eventsource.shop;

import io.pillopl.eventsource.shop.boundary.ShopItems;
import io.pillopl.eventsource.shop.domain.commands.Command;
import io.pillopl.eventsource.shop.domain.commands.MarkPaymentTimeout;
import io.pillopl.eventsource.shop.domain.commands.Order;
import io.pillopl.eventsource.shop.domain.commands.OrderWithTimeout;
import io.pillopl.eventsource.shop.domain.commands.Pay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@EnableBinding(Processor.class)
@Slf4j
public class Application {

    @Autowired
    ShopItems shopItems;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }

    @StreamListener(Sink.INPUT)
    public void commandStream(Command command) {
        log.info("Received command {}", command);
        if (command instanceof MarkPaymentTimeout) {
            shopItems.markPaymentTimeout((MarkPaymentTimeout) command);
        } else if (command instanceof Order) {
            shopItems.order((Order) command);
        } else if (command instanceof Pay) {
            shopItems.pay((Pay) command);
        }
    }
}
