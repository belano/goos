package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

public class ApplicationRunner {

    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
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
        driver.showSniperStatus(MainWindow.STATUS_JOINING);
    }

    public void showsSniperHasLostAuction() {
        driver.showSniperStatus(MainWindow.STATUS_LOST);
    }

    public void stop() {
        driver.dispose();
    }
}
