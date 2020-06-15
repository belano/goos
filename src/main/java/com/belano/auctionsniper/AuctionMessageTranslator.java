package com.belano.auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.belano.auctionsniper.AuctionEventListener.PriceSource;

public class AuctionMessageTranslator implements MessageListener {
    private final String sniperId;
    private final AuctionEventListener listener;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener) {
        this.sniperId = sniperId;
        this.listener = listener;
    }

    public void processMessage(Chat chat, Message message) {
        AuctionEvent event = AuctionEvent.from(message.getBody());
        String type = event.type();
        if ("CLOSE".equals(type)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(type)) {
            listener.currentPrice(
                    event.currentPrice(), event.increment(),
                    event.isFrom(sniperId));
        }
    }

    public static class AuctionEvent {
        private final Map<String, String> fields;

        private AuctionEvent(Map<String, String> fields) {
            this.fields = fields;
        }

        public static AuctionEvent from(String messageBody) {
            Map<String, String> fields = Stream.of(messageBody.split(";"))
                    .map(element -> element.split(":"))
                    .collect(Collectors.toMap(pair -> pair[0].trim(), pair -> pair[1].trim()));
            return new AuctionEvent(fields);
        }

        public String type() {
            return get("Event");
        }

        private String get(String fieldName) {
            return fields.get(fieldName);
        }

        private int getInt(String fieldName) {
            return Integer.parseInt(get(fieldName));
        }

        public int currentPrice() {
            return getInt("CurrentPrice");
        }

        public int increment() {
            return getInt("Increment");
        }

        public PriceSource isFrom(String sniperId) {
            return sniperId.equals(bidder())
                    ? PriceSource.FROM_SNIPER
                    : PriceSource.FROM_OTHER_BIDDER;
        }

        public String bidder() {
            return get("Bidder");
        }
    }
}
