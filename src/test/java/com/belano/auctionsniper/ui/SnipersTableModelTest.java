package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.AuctionSniper;
import com.belano.auctionsniper.SniperSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.util.Arrays;

import static com.belano.auctionsniper.ui.SnipersTableModel.textFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnipersTableModelTest {

    @Mock
    private TableModelListener listener;
    private SnipersTableModel model;

    @BeforeEach
    void setUp() {
        model = new SnipersTableModel();
        model.addTableModelListener(listener);
    }

    @Test
    void hasEnoughColumns() {
        assertThat(model.getColumnCount(), equalTo(Column.values().length));
    }

    @Test
    void setsUpColumnHeadings() {
        Arrays.stream(Column.values()).forEach(column -> assertThat("Unexpected column heading",
                model.getColumnName(column.ordinal()),
                equalTo(column.name)));
    }

    @Test
    void setsSniperValuesInColumns() {
        AuctionSniper sniper = aSniperFor("item-id");
        SniperSnapshot bidding = sniper.getSnapshot()
                .bidding(555, 666);

        model.sniperAdded(sniper);
        model.sniperStateChanged(bidding);

        assertRowMatches(0, bidding);
        Mockito.verify(listener, Mockito.atLeastOnce())
                .tableChanged(any(TableModelEvent.class));
    }

    @Test
    void notifiesListenersWhenAddingASniper() {
        assertThat(model.getRowCount(), equalTo(0));

        AuctionSniper sniper = aSniperFor("item-id");
        model.sniperAdded(sniper);

        assertThat(model.getRowCount(), equalTo(1));
        assertRowMatches(0, sniper.getSnapshot());
    }

    @Test
    public void holdsSniperInAdditionOrder() {
        model.sniperAdded(aSniperFor("item 0"));
        model.sniperAdded(aSniperFor("item 1"));

        assertColumnEquals(0, Column.ITEM_IDENTIFIER, "item 0");
        assertColumnEquals(1, Column.ITEM_IDENTIFIER, "item 1");
    }

    @Test
    public void updatesCorrectRowForSniper() {
        AuctionSniper sniper1 = aSniperFor("item 0");
        AuctionSniper sniper2 = aSniperFor("item 1");
        SniperSnapshot bidding = sniper2.getSnapshot()
                .bidding(22, 22);

        model.sniperAdded(sniper1);
        model.sniperAdded(sniper2);
        model.sniperStateChanged(bidding);

        assertRowMatches(1, bidding);
    }

    private static AuctionSniper aSniperFor(String itemId) {
        AuctionSniper sniper = mock(AuctionSniper.class);
        when(sniper.getSnapshot()).thenReturn(SniperSnapshot.joining(itemId));
        return sniper;
    }

    private void assertColumnEquals(int rowIndex, Column column, Object expected) {
        int columnIndex = column.ordinal();
        assertThat(expected, equalTo(model.getValueAt(rowIndex, columnIndex)));
    }

    private void assertRowMatches(int rowIndex, SniperSnapshot snapshot) {
        assertColumnEquals(rowIndex, Column.ITEM_IDENTIFIER, snapshot.itemId);
        assertColumnEquals(rowIndex, Column.LAST_PRICE, snapshot.lastPrice);
        assertColumnEquals(rowIndex, Column.LAST_BID, snapshot.lastBid);
        assertColumnEquals(rowIndex, Column.SNIPER_STATE, textFor(snapshot.state));
    }
}