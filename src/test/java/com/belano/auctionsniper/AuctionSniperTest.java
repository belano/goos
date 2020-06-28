package com.belano.auctionsniper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.belano.auctionsniper.AuctionEventListener.PriceSource.FROM_OTHER_BIDDER;
import static com.belano.auctionsniper.AuctionEventListener.PriceSource.FROM_SNIPER;
import static com.belano.auctionsniper.SniperState.*;
import static org.mockito.AdditionalMatchers.gt;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionSniperTest {

    private static final String ITEM_ID = "1234";
    public static final int STOP_PRICE = 1234;
    private final Item item = new Item(ITEM_ID, STOP_PRICE);

    @Mock
    private SniperListener listener;

    @Mock
    private Auction auction;

    private AuctionSniper sniper;

    @BeforeEach
    void setUp() {
        sniper = new AuctionSniper(item, auction);
        sniper.addSniperListener(listener);
    }

    @Test
    void reportsLostIfAuctionClosesImmediately() {
        sniper.auctionClosed();

        verify(listener, atLeastOnce())
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> LOST.equals(snapshot.state)));
    }

    @Test
    void reportsLostIfAuctionClosesWhenBidding() {
        final int price = 123;
        final int increment = 45;

        sniper.currentPrice(price, increment, FROM_OTHER_BIDDER);
        sniper.auctionClosed();

        verify(listener)
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> BIDDING.equals(snapshot.state)));
        verify(listener)
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> LOST.equals(snapshot.state)));
    }

    @Test
    void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, FROM_OTHER_BIDDER);

        verify(auction)
                .bid(bid);
        verify(listener, atLeastOnce())
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, BIDDING));
    }

    @Test
    void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 12, FROM_OTHER_BIDDER);
        sniper.currentPrice(135, 45, FROM_SNIPER);

        verify(listener, atLeastOnce())
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 135, BIDDING));
        verify(listener, atLeastOnce())
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WINNING));
    }

    @Test
    void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 45, FROM_SNIPER);
        sniper.auctionClosed();

        verify(listener)
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 0, WINNING));
        verify(listener)
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> WON.equals(snapshot.state)));
    }

    @Test
    public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        int bid = 123 + 45;
        sniper.currentPrice(123, 45, FROM_OTHER_BIDDER);
        sniper.currentPrice(2345, 23, FROM_OTHER_BIDDER);

        verify(auction, never()).bid(gt(STOP_PRICE));
        verify(listener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 2345, bid, LOSING));
    }

    @Test
    public void doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        sniper.currentPrice(1235, 45, FROM_OTHER_BIDDER);

        verify(auction, never()).bid(anyInt());
        verify(listener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 1235, 0, LOSING));
    }

    @Test
    public void reportsLostIfAuctionClosesWhenLosing() {
        sniper.currentPrice(1235, 45, FROM_OTHER_BIDDER);
        sniper.auctionClosed();

        verify(auction, never()).bid(anyInt());
        verify(listener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 1235, 0, LOST));
    }

    @Test
    public void continuesToBeLosingOnceStopPriceHasBeenReached() {
        sniper.currentPrice(1235, 45, FROM_OTHER_BIDDER);
        sniper.currentPrice(2000, 45, FROM_OTHER_BIDDER);

        verify(auction, never()).bid(anyInt());
        verify(listener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 1235, 0, LOSING));
        verify(listener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 2000, 0, LOSING));
    }

    @Test
    public void doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        sniper.currentPrice(123, 45, FROM_SNIPER);
        sniper.currentPrice(2000, 45, FROM_OTHER_BIDDER);

        verify(auction, never()).bid(gt(STOP_PRICE));
        verify(listener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 123, 0, WINNING));
        verify(listener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 2000, 0, LOSING));
    }

}
