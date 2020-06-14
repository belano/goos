package com.belano.auctionsniper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuction implements Auction {
    private final Chat chat;

    public XMPPAuction(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void join() {
        sendMessage(Main.JOIN_COMMAND_FORMAT);
    }

    @Override
    public void bid(int price) {
        sendMessage(String.format(Main.BID_COMMAND_FORMAT, price));
    }

    private void sendMessage(String message) {
        try {
            chat.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

}
