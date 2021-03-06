package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.belano.auctionsniper.AuctionEventListener.PriceSource;

public class AuctionMessageTranslator implements MessageListener {
    private final String sniperId;
    private final AuctionEventListener listener;
    private final XMPPFailureReporter failureReporter;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener, XMPPFailureReporter failureReporter) {
        this.sniperId = sniperId;
        this.listener = listener;
        this.failureReporter = failureReporter;
    }

    public void processMessage(Chat chat, Message message) {
        String messageBody = message.getBody();
        try {
            translate(messageBody);
        } catch (Exception parseException) {
            failureReporter.cannotTranslateMessage(sniperId, messageBody, parseException);
            listener.auctionFailed();
        }
    }

    private void translate(String messageBody) {
        AuctionEvent event = AuctionEvent.from(messageBody);
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
            Map<String, String> fields = parseFields(messageBody);
            return new AuctionEvent(fields);
        }

        private static Map<String, String> parseFields(String messageBody) {
            return Stream.of(messageBody.split(";"))
                    .map(element -> element.split(":"))
                    .collect(Collectors.toMap(pair -> pair[0].trim(), pair -> pair[1].trim()));
        }

        public String type() {
            return get("Event");
        }

        private String get(String fieldName) {
            return Objects.requireNonNull(fields.get(fieldName));
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
