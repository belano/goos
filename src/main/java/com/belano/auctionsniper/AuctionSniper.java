package com.belano.auctionsniper;

public class AuctionSniper implements AuctionEventListener {
    private final Auction auction;
    private final SniperListener listener;
    private SniperSnapshot snapshot;

    public AuctionSniper(String itemId, Auction auction, SniperListener listener) {
        this.auction = auction;
        this.listener = listener;
        this.snapshot = SniperSnapshot.joining(itemId);
    }

    public void auctionClosed() {
        snapshot = snapshot.closed();
        notifyChange();
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        switch (priceSource) {
            case FROM_SNIPER:
                snapshot = snapshot.winning(price);
                break;
            case FROM_OTHER_BIDDER:
                int bid = price + increment;
                auction.bid(bid);
                snapshot = snapshot.bidding(price, bid);
                break;
        }
        notifyChange();
    }

    private void notifyChange() {
        listener.sniperStateChanged(snapshot);
    }
}
