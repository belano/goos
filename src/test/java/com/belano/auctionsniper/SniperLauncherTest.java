package com.belano.auctionsniper;

import com.belano.auctionsniper.xmpp.AuctionHouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
public class SniperLauncherTest {

    @Mock
    private AuctionHouse auctionHouse;

    @Mock
    private SniperCollector collector;

    @Mock
    private Auction auction;

    private SniperLauncher sniperLauncher;

    @BeforeEach
    void setUp() {
        sniperLauncher = new SniperLauncher(auctionHouse, collector);
    }

    @Test
    void addsNewSniperToCollectorAndThenJoinsAuction() {
        Mockito.when(auctionHouse.auctionFor(any(Item.class)))
                .thenReturn(auction);
        InOrder orderedVerifier = inOrder(auction, collector);

        sniperLauncher.joinAuction(new Item("some-item-id", 0));

        orderedVerifier.verify(auction)
                .addAuctionEventListener(any(AuctionSniper.class));
        orderedVerifier.verify(collector)
                .addSniper(any(AuctionSniper.class));
        orderedVerifier.verify(auction)
                .join();
    }
}