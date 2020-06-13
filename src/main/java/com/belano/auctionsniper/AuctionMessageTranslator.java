package com.belano.auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuctionMessageTranslator implements MessageListener {
    private final AuctionMessageListener listener;

    public AuctionMessageTranslator(AuctionMessageListener listener) {
        this.listener = listener;
    }

    public void processMessage(Chat chat, Message message) {
        Map<String, String> event = unpackEventFrom(message);
        String type = event.get("Event");
        if ("CLOSE".equals(type)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(type)) {
            listener.currentPrice(
                    Integer.parseInt(event.get("CurrentPrice")),
                    Integer.parseInt(event.get("Increment"))
            );
        }
    }

    private Map<String, String> unpackEventFrom(Message message) {
        return Stream.of(message.getBody()
                .split(";"))
                .map(element -> element.split(":"))
                .collect(Collectors.toMap(pair -> pair[0].trim(), pair -> pair[1].trim()));
    }
}
