package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.SniperSnapshot;
import com.belano.auctionsniper.SniperState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.util.Arrays;

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
    void setsUpInitialSniperValues() {
        assertColumnEquals(Column.ITEM_IDENTIFIER, "");
        assertColumnEquals(Column.LAST_PRICE, 0);
        assertColumnEquals(Column.LAST_BID, 0);
        assertColumnEquals(Column.SNIPER_STATE, MainWindow.STATUS_JOINING);
    }

    @Test
    void setsSniperValuesInColumns() {
        model.sniperStateChanged(new SniperSnapshot("item-id", 123, 456, SniperState.BIDDING));

        assertColumnEquals(Column.ITEM_IDENTIFIER, "item-id");
        assertColumnEquals(Column.LAST_PRICE, 123);
        assertColumnEquals(Column.LAST_BID, 456);
        assertColumnEquals(Column.SNIPER_STATE, MainWindow.STATUS_BIDDING);

        Mockito.verify(listener)
                .tableChanged(any(TableModelEvent.class));
    }

    @Test
    void setsUpColumnHeadings() {
        Arrays.stream(Column.values()).forEach(column -> assertThat("Unexpected column heading",
                model.getColumnName(column.ordinal()),
                equalTo(column.name)));
    }

    private void assertColumnEquals(Column column, Object expected) {
        int rowIndex = 0;
        int columnIndex = column.ordinal();
        assertThat(expected, equalTo(model.getValueAt(rowIndex, columnIndex)));
    }
}