package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

public class ApplicationRunner {

    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID_REGEX = "^sniper@\\w+/Auction$";
    private final HostAndPort hostAndPort;
    private final AuctionSniperDriver driver = new AuctionSniperDriver(1000);

    public ApplicationRunner(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public void startBiddingIn(final FakeAuctionServer auction) {
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
        driver.showsSniperStatus(MainWindow.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        driver.showsSniperStatus(MainWindow.STATUS_LOST);
    }

    public void hasShownSniperIsBidding() {
        driver.showsSniperStatus(MainWindow.STATUS_BIDDING);
    }

    public void stop() {
        driver.dispose();
    }
}
