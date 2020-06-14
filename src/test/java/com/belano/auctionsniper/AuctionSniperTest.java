package com.belano.auctionsniper;

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
    void reportsLostWhenAuctionCloses() {
        auctionSniper.auctionClosed();

        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperLost();
    }

    @Test
    void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;

        auctionSniper.currentPrice(price, increment);

        Mockito.verify(auction).bid(price + increment);
        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperBidding();
    }
}
