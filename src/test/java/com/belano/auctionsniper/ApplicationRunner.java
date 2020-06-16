package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

public class ApplicationRunner {

    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID_REGEX = "^sniper@\\w+/Auction$";
    private final HostAndPort hostAndPort;
    private final AuctionSniperDriver driver = new AuctionSniperDriver(1000);

    private String itemId;

    public ApplicationRunner(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public void startBiddingIn(final FakeAuctionServer auction) {
        itemId = auction.getItemId();
        Thread thread = new Thread("Test application") {
            @Override
            public void run() {
                try {
                    Main.main(hostAndPort.getHostText(),
                            String.valueOf(hostAndPort.getPort()),
                            SNIPER_ID,
                            SNIPER_PASSWORD,
                            auction.getItemId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitles();
        driver.showsSniperStatus(MainWindow.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        driver.showsSniperStatus(MainWindow.STATUS_LOST);
    }

    public void showsSniperHasWonAuction(int lastPrice) {
        driver.showsSniperStatus(itemId, lastPrice, lastPrice, MainWindow.STATUS_WON);
    }

    public void hasShownSniperIsBidding(int lastPrice, int lastBid) {
        driver.showsSniperStatus(itemId, lastPrice, lastBid, MainWindow.STATUS_BIDDING);
    }

    public void hasShownSniperIsWinning(int winningBid) {
        driver.showsSniperStatus(itemId, winningBid, winningBid, MainWindow.STATUS_WINNING);
    }

    public void stop() {
        driver.dispose();
    }
}
