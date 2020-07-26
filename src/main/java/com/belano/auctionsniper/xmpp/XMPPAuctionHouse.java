package com.belano.auctionsniper.xmpp;

import static org.apache.commons.io.FilenameUtils.getFullPath;

import com.belano.auctionsniper.Auction;
import com.belano.auctionsniper.Item;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.belano.auctionsniper.AuctionLogDriver.LOG_FILE_NAME;

public class XMPPAuctionHouse implements AuctionHouse {

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private static final String LOGGER_NAME = "XMPPLogger";
    private final XMPPConnection connection;
    private final XMPPFailureReporter failureReporter;

    private XMPPAuctionHouse(XMPPConnection connection) throws XMPPAuctionException {
        this.connection = connection;
        this.failureReporter = new LoggingXMPPFailureReporter(makeLogger());
    }

    private Logger makeLogger() throws XMPPAuctionException {
        Logger logger = Logger.getLogger(LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.addHandler(simpleFilerHandler());
        return logger;
    }

    private FileHandler simpleFilerHandler() throws XMPPAuctionException {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_NAME);
            handler.setFormatter(new SimpleFormatter());
            return handler;
        } catch (Exception e) {
            throw new XMPPAuctionException("Could nto create logger FileHandler = "
                    + getFullPath(LOG_FILE_NAME), e);
        }
    }

    public static AuctionHouse connectTo(String hostname, String port, String username, String password) throws XMPPException, XMPPAuctionException {
        ConnectionConfiguration config = new ConnectionConfiguration(hostname, Integer.parseInt(port));
        XMPPConnection connection = new XMPPConnection(config);
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public Auction auctionFor(Item item) {
        return new XMPPAuction(connection, auctionId(item), failureReporter);
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    private String auctionId(Item item) {
        // "auction-item-xxxxx@serviceName/Auction"
        return String.format(AUCTION_ID_FORMAT, item.identifier, connection.getServiceName());
    }
}
