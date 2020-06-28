package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.AuctionSniper;
import com.belano.auctionsniper.Defect;
import com.belano.auctionsniper.PortfolioListener;
import com.belano.auctionsniper.SniperListener;
import com.belano.auctionsniper.SniperSnapshot;
import com.belano.auctionsniper.SniperState;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SnipersTableModel extends AbstractTableModel implements SniperListener, PortfolioListener {
    private static final String[] STATUS_TEXT = {
            "JOINING", "BIDDING", "WINNING", "LOSING", "LOST", "WON"
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

    @Override
    public void sniperAdded(AuctionSniper sniper) {
        addSniperSnapshot(sniper.getSnapshot());
        sniper.addSniperListener(new SwingThreadSniperListener(this));
    }

    private void addSniperSnapshot(SniperSnapshot snapshot) {
        snapshots.add(snapshot);
        int row = snapshots.size() - 1;
        fireTableRowsInserted(row, row);
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
