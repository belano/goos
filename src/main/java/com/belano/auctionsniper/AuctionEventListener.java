package com.belano.auctionsniper;

import java.util.EventListener;

public interface AuctionEventListener extends EventListener {
    enum PriceSource {
        FROM_SNIPER, FROM_OTHER_BIDDER
    }

    void auctionClosed();

    void currentPrice(int currentPrice, int increment, PriceSource priceSource);

    void auctionFailed();
}
