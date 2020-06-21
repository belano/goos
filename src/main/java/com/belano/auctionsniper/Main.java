package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import com.belano.auctionsniper.ui.SnipersTableModel;
import com.belano.auctionsniper.xmpp.XMPPAuction;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import static javax.swing.SwingUtilities.invokeAndWait;

public class Main {

    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_USERNAME = 2;
    private static final int ARG_PASSWORD = 3;

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    public static final String JOIN_COMMAND_FORMAT = "";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private final SnipersTableModel snipers = new SnipersTableModel();
    private MainWindow ui;
    private Set<Auction> notToBeGCd = new HashSet<>();

    public static void main(String... args) throws Exception {
        Main main = new Main();
        String hostname = args[ARG_HOSTNAME];
        String port = args[ARG_PORT];
        String username = args[ARG_USERNAME];
        String password = args[ARG_PASSWORD];
        XMPPConnection connection = connectTo(hostname, port, username, password);
        main.disconnectWhenUICloses(connection);
        main.addUserRequestListenerFor(connection);
    }

    public Main() throws Exception {
        startUserInterface();
    }

    private void addUserRequestListenerFor(XMPPConnection connection) {
        ui.addUserRequestListener(itemId -> {
            snipers.addSniper(SniperSnapshot.joining(itemId));
            Auction auction = new XMPPAuction(connection, itemId);
            notToBeGCd.add(auction);
            auction.addAuctionEventListener(
                    new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers))
            );
            auction.join();
        });
    }

    private void disconnectWhenUICloses(XMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    private void startUserInterface() throws Exception {
        invokeAndWait(() -> ui = new MainWindow(snipers));
    }

    private static XMPPConnection connectTo(String hostname, String port, String username, String password) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(hostname, Integer.parseInt(port));
        XMPPConnection connection = new XMPPConnection(config);
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return connection;
    }

    /**
     * Decorator that pushes updates onto the Swing event thread, delegating to SnipersTableModel
     */
    public static class SwingThreadSniperListener implements SniperListener {

        private final SnipersTableModel snipers;

        public SwingThreadSniperListener(SnipersTableModel snipers) {
            this.snipers = snipers;
        }

        @Override
        public void sniperStateChanged(SniperSnapshot snapshot) {
            snipers.sniperStateChanged(snapshot);
        }

    }

}
