package com.belano.auctionsniper;

public class AuctionSniper implements AuctionEventListener {
    private final Auction auction;
    private final SniperListener sniperListener;

    public AuctionSniper(Auction auction, SniperListener sniperListener) {
        this.auction = auction;
        this.sniperListener = sniperListener;
    }

    public void auctionClosed() {
        sniperListener.sniperLost();
    }

    @Override
    public void currentPrice(int currentPrice, int increment) {
        auction.bid(currentPrice + increment);
        sniperListener.sniperBidding();
    }
}
