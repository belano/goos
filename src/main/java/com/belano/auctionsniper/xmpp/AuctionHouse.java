package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.Auction;

public interface AuctionHouse {
    Auction auctionFor(String itemId);

    void disconnect();
}
