package com.belano.auctionsniper;

public interface AuctionMessageListener {
    void auctionClosed();

    void currentPrice(int currentPrice, int increment);
}
