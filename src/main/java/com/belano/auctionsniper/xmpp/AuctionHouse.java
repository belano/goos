package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.Auction;
import com.belano.auctionsniper.Item;

public interface AuctionHouse {
    Auction auctionFor(Item item);

    void disconnect();
}
