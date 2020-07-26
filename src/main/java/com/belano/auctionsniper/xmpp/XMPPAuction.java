package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.Auction;
import com.belano.auctionsniper.AuctionEventAnnouncer;
import com.belano.auctionsniper.AuctionEventListener;
import com.belano.auctionsniper.Main;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuction implements Auction {
    private final AuctionEventAnnouncer auctionEventListeners = new AuctionEventAnnouncer();
    private final Chat chat;

    public XMPPAuction(XMPPConnection connection, String auctionId) {
        AuctionMessageTranslator translator = translatorFor(connection);
        chat = connection.getChatManager()
                .createChat(
                        auctionId,
                        translator
                );
        addAuctionEventListener(chatDisconnectorFor(translator));
    }

    private AuctionMessageTranslator translatorFor(XMPPConnection connection) {
        return new AuctionMessageTranslator(
                getUsernameFrom(connection),
                auctionEventListeners);
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

    private AuctionEventListener chatDisconnectorFor(final AuctionMessageTranslator translator) {
        return new AuctionEventListener() {
            @Override public void auctionClosed() {}
            @Override public void currentPrice(int price, int increment, PriceSource fromSniper) {}

            @Override public void auctionFailed() {
                chat.removeMessageListener(translator);
            }
        };
    }
}
