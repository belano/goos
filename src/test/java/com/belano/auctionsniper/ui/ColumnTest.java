package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.SniperSnapshot;
import com.belano.auctionsniper.SniperState;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ColumnTest {

    private static final String ITEM_ID = "item-id";
    private static final int LAST_PRICE = 123;
    private static final int LAST_BID = 456;
    private static final SniperSnapshot snapshot =
            new SniperSnapshot(ITEM_ID, LAST_PRICE, LAST_BID, SniperState.BIDDING);

    @Test
    void columnReturnValuesInSnapshot() {
        assertThat("Unexpected item id", Column.ITEM_IDENTIFIER.valueIn(snapshot), equalTo(ITEM_ID));
        assertThat("Unexpected last price", Column.LAST_PRICE.valueIn(snapshot), equalTo(LAST_PRICE));
        assertThat("Unexpected last bid", Column.LAST_BID.valueIn(snapshot), equalTo(LAST_BID));
        assertThat("Unexpected state", Column.SNIPER_STATE.valueIn(snapshot), equalTo(MainWindow.STATUS_BIDDING));
    }
}