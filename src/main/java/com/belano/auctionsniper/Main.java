package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import javax.swing.SwingUtilities;

import static javax.swing.SwingUtilities.invokeAndWait;

public class Main {

    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_USERNAME = 2;
    private static final int ARG_PASSWORD = 3;
    private static final int ARG_ITEM_ID = 4;

    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    private MainWindow ui;
    private Chat notToBeGCd;

    public static void main(String... args) throws Exception {
        Main main = new Main();
        String hostname = args[ARG_HOSTNAME];
        String port = args[ARG_PORT];
        String username = args[ARG_USERNAME];
        String password = args[ARG_PASSWORD];
        String itemId = args[ARG_ITEM_ID];
        main.joinAuction(
                connectTo(hostname, port, username, password),
                itemId
        );
    }

    public Main() throws Exception {
        startUserInterface();
    }

    private void joinAuction(XMPPConnection connection, String itemId) throws XMPPException {
        final Chat chat = connection.getChatManager()
                .createChat(auctionId(itemId, connection), (aChat, message) -> {
                    SwingUtilities.invokeLater(() -> ui.showStatus(MainWindow.STATUS_LOST));
                });
        this.notToBeGCd = chat;
        chat.sendMessage(new Message());
    }

    private void startUserInterface() throws Exception {
        invokeAndWait(() -> ui = new MainWindow());
    }

    private static XMPPConnection connectTo(String hostname, String port, String username, String password) throws XMPPException {
        ConnectionConfiguration config = new ConnectionConfiguration(hostname, Integer.parseInt(port));
        XMPPConnection connection = new XMPPConnection(config);
        connection.connect();
        connection.login(username, password, AUCTION_RESOURCE);
        return connection;
    }

    private static String auctionId(String itemId, XMPPConnection connection) {
        // "auction-item-xxxxx@ccd85df8bc69/Auction"
        return String.format(AUCTION_ID_FORMAT, itemId, connection.getServiceName());
    }
}
