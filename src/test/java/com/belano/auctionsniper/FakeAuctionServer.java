package com.belano.auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

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
        connection.login(String.format(Main.ITEM_ID_AS_LOGIN, getItemId()),
                AUCTION_PASSWORD, Main.AUCTION_RESOURCE);
        connection.getChatManager()
                .addChatListener((chat, createdLocally) -> {
                    currentChat = chat;
                    chat.addMessageListener(messageListener);
                });
    }

    public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
        messageListener.receivesAMessage();
    }

    public void announceClosed() throws XMPPException {
        currentChat.sendMessage(new Message());
    }

    public void stop() {
        connection.disconnect();
    }

    public String getItemId() {
        return this.itemId;
    }
}
