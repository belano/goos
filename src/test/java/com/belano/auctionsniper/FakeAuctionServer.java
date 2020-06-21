package com.belano.auctionsniper;

import org.hamcrest.Matcher;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

import static com.belano.auctionsniper.xmpp.XMPPAuctionHouse.AUCTION_RESOURCE;
import static com.belano.auctionsniper.xmpp.XMPPAuctionHouse.ITEM_ID_AS_LOGIN;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

public class FakeAuctionServer {
    private static final String AUCTION_PASSWORD = "auction";
    private final String itemId;
    private final XMPPConnection connection;
    private final SingleMessageListener messageListener = new SingleMessageListener();
    private Chat currentChat;

    public FakeAuctionServer(HostAndPort hostAndPort, String itemId) {
        this.itemId = itemId;
        this.connection = createConnection(hostAndPort);
    }

    private static XMPPConnection createConnection(HostAndPort hostAndPort) {
        ConnectionConfiguration config = new ConnectionConfiguration(hostAndPort.getHostText(), hostAndPort.getPort());
        return new XMPPConnection(config);
    }

    public void startSellingItem() throws XMPPException {
        connection.connect();
        connection.login(format(ITEM_ID_AS_LOGIN, getItemId()),
                AUCTION_PASSWORD, AUCTION_RESOURCE);
        connection.getChatManager()
                .addChatListener((chat, createdLocally) -> {
                    currentChat = chat;
                    chat.addMessageListener(messageListener);
                });
    }

    public void hasReceivedJoinRequestFrom(String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, equalTo(Main.JOIN_COMMAND_FORMAT));
    }

    public void hasReceivedBid(int bid, String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, equalTo(format(Main.BID_COMMAND_FORMAT, bid)));
    }

    private void receivesAMessageMatching(String sniperId, Matcher<? super String> messageMatcher) throws InterruptedException {
        messageListener.receivesAMessage(messageMatcher);
        assertThat(currentChat.getParticipant(), matchesPattern(sniperId));
    }

    public void announceClosed() throws XMPPException {
        currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;");
    }

    public void stop() {
        connection.disconnect();
    }

    public String getItemId() {
        return this.itemId;
    }

    public void reportPrice(int price, int increment, String bidder) throws XMPPException {
        currentChat.sendMessage(
                format("SOLVersion: 1.1; Event: PRICE; "
                                + "CurrentPrice: %d; Increment: %d; Bidder: %s",
                        price, increment, bidder));
    }

}
