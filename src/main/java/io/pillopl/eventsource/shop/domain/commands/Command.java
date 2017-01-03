package io.pillopl.eventsource.shop.domain.commands;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type", defaultImpl = VoidCommand.class)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "item.order", value = Order.class),
        @JsonSubTypes.Type(name = "item.pay", value = Pay.class),
        @JsonSubTypes.Type(name = "item.markPaymentTimeout", value = MarkPaymentTimeout.class)
})
public interface Command {


}

class VoidCommand implements Command {

}

