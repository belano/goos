package com.belano.auctionsniper;

import static com.belano.auctionsniper.AuctionEventListener.PriceSource;
import static org.mockito.ArgumentMatchers.anyInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuctionSniperTest {

    @Mock
    private SniperListener sniperListener;

    @Mock
    private Auction auction;

    private AuctionSniper auctionSniper;

    @BeforeEach
    void setUp() {
        auctionSniper = new AuctionSniper(auction, sniperListener);
    }

    @Test
    void reportsLostIfAuctionClosesImmediately() {
        auctionSniper.auctionClosed();

        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperLost();
    }

    @Test
    void reportsLostIfAuctionClosesWhenBidding() {
        auctionSniper.currentPrice(123, 45, PriceSource.FROM_OTHER_BIDDER);
        auctionSniper.auctionClosed();

        Mockito.verify(sniperListener).sniperBidding();
        Mockito.verify(sniperListener).sniperLost();
    }

    @Test
    void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;

        auctionSniper.currentPrice(price, increment, PriceSource.FROM_OTHER_BIDDER);

        Mockito.verify(auction).bid(price + increment);
        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperBidding();
    }

    @Test
    void reportsWinningWhenCurrentPriceComesFromSniper() {
        final int price = 1001;
        final int increment = 25;

        auctionSniper.currentPrice(price, increment, PriceSource.FROM_SNIPER);

        Mockito.verify(sniperListener).sniperWinning();
        Mockito.verify(auction, Mockito.never()).bid(anyInt());
    }

    @Test
    void reportsWonIfAuctionClosesWhenWinning() {
        auctionSniper.currentPrice(123, 45, PriceSource.FROM_SNIPER);
        auctionSniper.auctionClosed();

        Mockito.verify(sniperListener).sniperWinning();
        Mockito.verify(sniperListener).sniperWon();
    }
}
