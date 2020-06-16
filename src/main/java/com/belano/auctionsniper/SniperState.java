package com.belano.auctionsniper;

public enum SniperState {
    JOINING {
        @Override
        public SniperState whenAuctionClosed() {
            return LOST;
        }
    },
    BIDDING {
        @Override
        public SniperState whenAuctionClosed() {
            return LOST;
        }
    },
    WINNING {
        @Override
        public SniperState whenAuctionClosed() {
            return WON;
        }
    },
    LOST,
    WON;

    public SniperState whenAuctionClosed() {
        throw new Defect("Auction is already closed");
    }

    public static class Defect extends RuntimeException {
        public Defect(String message) {
            super(message);
        }
    }
}
