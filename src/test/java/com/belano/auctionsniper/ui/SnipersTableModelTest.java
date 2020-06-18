package com.belano.auctionsniper.ui;

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
        SniperSnapshot joining = SniperSnapshot.joining("item-id");
        SniperSnapshot bidding = joining.bidding(555, 666);

        model.addSniper(joining);
        model.sniperStateChanged(bidding);

        assertRowMatches(0, bidding);
        Mockito.verify(listener, Mockito.atLeastOnce())
                .tableChanged(any(TableModelEvent.class));
    }

    @Test
    void notifiesListenersWhenAddingASniper() {
        assertThat(model.getRowCount(), equalTo(0));

        SniperSnapshot joining = SniperSnapshot.joining("item-123");
        model.addSniper(joining);

        assertThat(model.getRowCount(), equalTo(1));
        assertRowMatches(0, joining);
    }

    @Test
    public void holdsSniperInAdditionOrder() {
        model.addSniper(SniperSnapshot.joining("item 0"));
        model.addSniper(SniperSnapshot.joining("item 1"));

        assertColumnEquals(0, Column.ITEM_IDENTIFIER, "item 0");
        assertColumnEquals(1, Column.ITEM_IDENTIFIER, "item 1");
    }

    @Test
    public void updatesCorrectRowForSniper() {
        SniperSnapshot joining1 = SniperSnapshot.joining("item 0");
        SniperSnapshot joining2 = SniperSnapshot.joining("item 1");
        SniperSnapshot bidding = joining2.bidding(22, 22);

        model.addSniper(joining1);
        model.addSniper(joining2);
        model.sniperStateChanged(bidding);

        assertRowMatches(1, bidding);
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