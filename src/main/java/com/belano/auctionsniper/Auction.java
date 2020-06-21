package com.belano.auctionsniper;

public interface Auction {
    void join();
    void bid(int price);
    void addAuctionEventListener(AuctionEventListener sniper);
}
