package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import com.belano.auctionsniper.xmpp.AuctionHouse;
import com.belano.auctionsniper.xmpp.XMPPAuctionHouse;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.SwingUtilities.invokeAndWait;

public class Main {

    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_USERNAME = 2;
    private static final int ARG_PASSWORD = 3;

    public static final String JOIN_COMMAND_FORMAT = "";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private final SniperPortfolio portfolio = new SniperPortfolio();
    private MainWindow ui;

    public static void main(String... args) throws Exception {
        Main main = new Main();
        String hostname = args[ARG_HOSTNAME];
        String port = args[ARG_PORT];
        String username = args[ARG_USERNAME];
        String password = args[ARG_PASSWORD];
        AuctionHouse auctionHouse = XMPPAuctionHouse.connectTo(hostname, port, username, password);
        main.disconnectWhenUICloses(auctionHouse);
        main.addUserRequestListenerFor(auctionHouse);
    }

    public Main() throws Exception {
        startUserInterface();
    }

    private void addUserRequestListenerFor(AuctionHouse auctionHouse) {
        ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio));
    }

    private void disconnectWhenUICloses(AuctionHouse auctionHouse) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auctionHouse.disconnect();
            }
        });
    }

    private void startUserInterface() throws Exception {
        invokeAndWait(() -> ui = new MainWindow(portfolio));
    }

}
