package com.belano.auctionsniper;

import java.util.HashSet;
import java.util.Set;

public class AuctionEventAnnouncer implements AuctionEventListener {
    private final Set<AuctionEventListener> listeners = new HashSet<>();

    public void addListener(AuctionEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void auctionClosed() {
        listeners.forEach(AuctionEventListener::auctionClosed);
    }

    @Override
    public void currentPrice(int currentPrice, int increment, PriceSource priceSource) {
        listeners.forEach(listener -> listener.currentPrice(currentPrice, increment, priceSource));
    }
}
