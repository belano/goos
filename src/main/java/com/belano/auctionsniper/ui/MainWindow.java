package com.belano.auctionsniper.ui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;

public class MainWindow extends JFrame {

    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String STATUS_JOINING = "JOINING";
    public static final String STATUS_LOST = "LOST";
    public static final String STATUS_BIDDING = "BIDDING";
    public static final String STATUS_WINNING = "WINNING";
    public static final String STATUS_WON = "WON";
    public static final String APPLICATION_TITLE = "Auction Sniper";
    private static final String SNIPERS_TABLE_NAME = "Snipers";

    private final SnipersTableModel snipers;

    public MainWindow(SnipersTableModel snipers) throws HeadlessException {
        super(APPLICATION_TITLE);
        this.snipers = snipers;
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JTable makeSnipersTable() {
        JTable snipersTable = new JTable(snipers);
        snipersTable.setName(SNIPERS_TABLE_NAME);
        return snipersTable;
    }

    private void fillContentPane(JTable snipersTable) {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

}
