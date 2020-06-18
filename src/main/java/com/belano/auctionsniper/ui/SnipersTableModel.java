package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.Defect;
import com.belano.auctionsniper.SniperListener;
import com.belano.auctionsniper.SniperSnapshot;
import com.belano.auctionsniper.SniperState;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SnipersTableModel extends AbstractTableModel implements SniperListener {
    private static final String[] STATUS_TEXT = {
            "JOINING", "BIDDING", "WINNING", "LOST", "WON"
    };
    private final List<SniperSnapshot> snapshots = new ArrayList<>();

    @Override
    public int getRowCount() {
        return snapshots.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return Column.at(columnIndex)
                .valueIn(snapshots.get(rowIndex));
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
        int rowMatching = rowMatching(newSniperSnapshot);
        snapshots.set(rowMatching, newSniperSnapshot);
        fireTableRowsUpdated(rowMatching, rowMatching);
    }

    private int rowMatching(SniperSnapshot newSnapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (newSnapshot.isForSameItemAs(snapshots.get(i))) {
                return i;
            }
        }
        throw new Defect("Cannot find match for " + newSnapshot);
    }

    public void addSniper(SniperSnapshot snapshot) {
        snapshots.add(snapshot);
    }

}
