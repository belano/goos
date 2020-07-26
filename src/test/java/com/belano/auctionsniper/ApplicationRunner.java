package com.belano.auctionsniper;

import com.belano.auctionsniper.ui.MainWindow;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.belano.auctionsniper.ui.SnipersTableModel.textFor;
import static org.hamcrest.CoreMatchers.containsString;

public class ApplicationRunner {

    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID_REGEX = "^sniper@\\w+/Auction$";
    private final HostAndPort hostAndPort;
    private final AuctionSniperDriver driver = new AuctionSniperDriver(1000);
    private final AuctionLogDriver logDriver = new AuctionLogDriver();

    public ApplicationRunner(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public void startBiddingIn(final FakeAuctionServer... auctions) {
        startBiddingWithStopPrice(Integer.MAX_VALUE, auctions);
    }

    public void startBiddingWithStopPrice(int stopPrice, FakeAuctionServer... auctions) {
        startSniper(auctions);
        Arrays.stream(auctions)
                .map(FakeAuctionServer::getItemId)
                .forEach(itemId -> {
                    driver.startBiddingFor(itemId, stopPrice);
                    driver.showsSniperStatus(itemId, 0, 0, textFor(SniperState.JOINING));
                });
    }

    private void startSniper(FakeAuctionServer[] auctions) {
        logDriver.clearLog();
        Thread thread = new Thread("Test application") {
            @Override
            public void run() {
                try {
                    Main.main(arguments(auctions));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitles();
    }

    private String[] arguments(FakeAuctionServer... auctions) {
        String[] arguments = new String[auctions.length + 4];
        arguments[0] = hostAndPort.getHostText();
        arguments[1] = String.valueOf(hostAndPort.getPort());
        arguments[2] = SNIPER_ID;
        arguments[3] = SNIPER_PASSWORD;
        IntStream.range(0, auctions.length)
                .forEach(i -> arguments[i + 4] = auctions[i].getItemId());
        return arguments;
    }

    public void showsSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.LOST));
    }

    public void showsSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastPrice, textFor(SniperState.WON));
    }

    public void showsSniperHasFailed(FakeAuctionServer auction) {
        driver.showsSniperStatus(auction.getItemId(), 0, 0, textFor(SniperState.FAILED));
    }

    public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.BIDDING));
    }

    public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        driver.showsSniperStatus(auction.getItemId(), winningBid, winningBid, textFor(SniperState.WINNING));
    }

    public void hasShownSniperIsLosing(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, textFor(SniperState.LOSING));
    }

    public void stop() {
        driver.dispose();
    }

    public void reportsInvalidMessage(FakeAuctionServer auction, String brokenMessage) throws IOException {
        logDriver.hasEntry(containsString(brokenMessage));
    }
}
