package com.belano.auctionsniper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.belano.auctionsniper.AuctionEventListener.PriceSource;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
public class AuctionSniperTest {

    private static final String ITEM_ID = "1234";

    @Mock
    private SniperListener listener;

    @Mock
    private Auction auction;

    private AuctionSniper sniper;

    @BeforeEach
    void setUp() {
        sniper = new AuctionSniper(ITEM_ID, auction, listener);
    }

    @Test
    void reportsLostIfAuctionClosesImmediately() {
        sniper.auctionClosed();

        Mockito.verify(listener, Mockito.atLeastOnce())
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> SniperState.LOST.equals(snapshot.state)));
    }

    @Test
    void reportsLostIfAuctionClosesWhenBidding() {
        final int price = 123;
        final int increment = 45;

        sniper.currentPrice(price, increment, PriceSource.FROM_OTHER_BIDDER);
        sniper.auctionClosed();

        Mockito.verify(listener)
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> SniperState.BIDDING.equals(snapshot.state)));
        Mockito.verify(listener)
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> SniperState.LOST.equals(snapshot.state)));
    }

    @Test
    void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, PriceSource.FROM_OTHER_BIDDER);

        Mockito.verify(auction)
                .bid(bid);
        Mockito.verify(listener, Mockito.atLeastOnce())
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }

    @Test
    void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 12, PriceSource.FROM_OTHER_BIDDER);
        sniper.currentPrice(135, 45, PriceSource.FROM_SNIPER);

        Mockito.verify(listener, Mockito.atLeastOnce())
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 135, SniperState.BIDDING));
        Mockito.verify(listener, Mockito.atLeastOnce())
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING));
    }

    @Test
    void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 45, PriceSource.FROM_SNIPER);
        sniper.auctionClosed();

        Mockito.verify(listener)
                .sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 0, SniperState.WINNING));
        Mockito.verify(listener)
                .sniperStateChanged(argThat((SniperSnapshot snapshot) -> SniperState.WON.equals(snapshot.state)));
    }

}
