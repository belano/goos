package com.belano.auctionsniper.ui;

import java.util.EventListener;

public interface UserRequestListener extends EventListener {
    void joinAuction(String itemId);
}
