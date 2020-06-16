package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.SniperListener;
import com.belano.auctionsniper.SniperSnapshot;
import com.belano.auctionsniper.SniperState;

import javax.swing.table.AbstractTableModel;

public class SnipersTableModel extends AbstractTableModel implements SniperListener {
    private static final String[] STATUS_TEXT = {
            "JOINING", "BIDDING", "WINNING", "LOST", "WON"
    };
    private static final SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0, SniperState.JOINING);
    private SniperSnapshot sniperSnapshot = STARTING_UP;

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return Column.at(columnIndex)
                .valueIn(sniperSnapshot);
    }

    @Override
    public String getColumnName(int column) {
        return Column.at(column).name;
    }

    public static String textFor(SniperState state) {
        return STATUS_TEXT[state.ordinal()];
    }

    @Override
    public void sniperStateChanged(SniperSnapshot newSniperSnapshot) {
        sniperSnapshot = newSniperSnapshot;
        fireTableRowsUpdated(0, 0);
    }
}
