package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.Auction;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class XMPPAuctionHouse implements AuctionHouse {

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private final XMPPConnection connection;

    private XMPPAuctionHouse(XMPPConnection connection) {
        this.connection = connection;
    }

    public static AuctionHouse connectTo(String hostname, String port, String username, String password) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(hostname, Integer.parseInt(port));
        XMPPConnection connection = new XMPPConnection(config);
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public Auction auctionFor(String itemId) {
        return new XMPPAuction(connection, auctionId(itemId));
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    private String auctionId(String itemId) {
        // "auction-item-xxxxx@serviceName/Auction"
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
    }
}
