package com.belano.auctionsniper;

public class AuctionSniper implements AuctionEventListener {
    private final Auction auction;
    private final SniperListener sniperListener;
    private boolean isWinning;

    public AuctionSniper(Auction auction, SniperListener sniperListener) {
        this.auction = auction;
        this.sniperListener = sniperListener;
    }

    public void auctionClosed() {
        if (isWinning) {
            sniperListener.sniperWon();
        } else {
            sniperListener.sniperLost();
        }
    }

    @Override
    public void currentPrice(int currentPrice, int increment, PriceSource priceSource) {
        isWinning = priceSource == PriceSource.FROM_SNIPER;
        if (isWinning) {
            sniperListener.sniperWinning();
        } else {
            auction.bid(currentPrice + increment);
            sniperListener.sniperBidding();
        }
    }
}
