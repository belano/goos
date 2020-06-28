package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.Item;
import com.belano.auctionsniper.SniperPortfolio;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class MainWindow extends JFrame {

    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String APPLICATION_TITLE = "Auction Sniper";
    public static final String NEW_ITEM_ID_NAME = "item id";
    public static final String NEW_ITEM_STOP_PRICE_NAME = "stop price";
    public static final String JOIN_BUTTON_NAME = "join button";
    private static final String SNIPERS_TABLE_NAME = "Snipers";

    private final Set<UserRequestListener> userRequests = new HashSet<>();

    public MainWindow(SniperPortfolio portfolio) throws HeadlessException {
        super(APPLICATION_TITLE);
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable(portfolio), makeControls());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JTable makeSnipersTable(SniperPortfolio snipers) {
        SnipersTableModel model = new SnipersTableModel();
        snipers.addPortfolioListener(model);
        JTable snipersTable = new JTable(model);
        snipersTable.setName(SNIPERS_TABLE_NAME);
        return snipersTable;
    }

    private JPanel makeControls() {
        JPanel controls = new JPanel(new FlowLayout());
        final JTextField itemIdField = new JTextField();
        itemIdField.setColumns(25);
        itemIdField.setName(NEW_ITEM_ID_NAME);
        controls.add(itemIdField);

        final JTextField stopPriceField = new JTextField();
        stopPriceField.setColumns(20);
        stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME);
        controls.add(itemIdField);
        controls.add(stopPriceField);

        JButton joinAuctionButton = new JButton("Join Auction");
        joinAuctionButton.setName(JOIN_BUTTON_NAME);
        joinAuctionButton.addActionListener(e -> userRequests.forEach(new Consumer<UserRequestListener>() {
            @Override
            public void accept(UserRequestListener userRequestListener) {
                userRequestListener.joinAuction(new Item(itemId(), stopPrice()));
            }

            private int stopPrice() {
                return Integer.parseInt(stopPriceField.getText());
            }

            private String itemId() {
                return itemIdField.getText();
            }
        }));
        controls.add(joinAuctionButton);

        return controls;
    }

    private void fillContentPane(JTable snipersTable, JPanel controlPanel) {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(controlPanel, BorderLayout.PAGE_START);
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    public void addUserRequestListener(UserRequestListener userRequestListener) {
        userRequests.add(userRequestListener);
    }
}
