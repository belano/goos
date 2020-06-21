package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.Auction;
import com.belano.auctionsniper.AuctionEventAnnouncer;
import com.belano.auctionsniper.AuctionEventListener;
import com.belano.auctionsniper.Main;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import static com.belano.auctionsniper.Main.AUCTION_ID_FORMAT;

public class XMPPAuction implements Auction {
    private final AuctionEventAnnouncer auctionEventListeners = new AuctionEventAnnouncer();
    private final Chat chat;

    public XMPPAuction(XMPPConnection connection, String itemId) {
        chat = connection.getChatManager()
                .createChat(
                        auctionId(itemId, connection),
                        new AuctionMessageTranslator(
                                getUsernameFrom(connection),
                                auctionEventListeners)
                );
    }

    @Override
    public void join() {
        sendMessage(Main.JOIN_COMMAND_FORMAT);
    }

    @Override
    public void bid(int price) {
        sendMessage(String.format(Main.BID_COMMAND_FORMAT, price));
    }

    @Override
    public void addAuctionEventListener(AuctionEventListener sniper) {
        auctionEventListeners.addListener(sniper);
    }

    private void sendMessage(String message) {
        try {
            chat.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private static String getUsernameFrom(XMPPConnection connection) {
        return connection.getUser()
                .substring(0, connection.getUser()
                        .indexOf('@'));
    }

    private static String auctionId(String itemId, XMPPConnection connection) {
        // "auction-item-xxxxx@serviceName/Auction"
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
    }

}
