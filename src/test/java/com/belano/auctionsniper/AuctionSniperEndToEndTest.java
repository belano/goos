package com.belano.auctionsniper;

import com.belano.testcontainers.OpenfireContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Testcontainers
@TestInstance(PER_CLASS)
@Tag("integration")
public class AuctionSniperEndToEndTest {

    private static final Logger logger = LoggerFactory.getLogger(AuctionSniperEndToEndTest.class);

    @Container
    private static final OpenfireContainer container = new OpenfireContainer();
    private static HostAndPort hostAndPort;

    private ApplicationRunner application;
    private FakeAuctionServer auction;
    private FakeAuctionServer anotherAuction;

    @BeforeAll
    public void setUpOpenfire() {
        hostAndPort = HostAndPort.fromParts(container.getHost(), container.getMappedPort(5222));
        logger.info(">> Container id: {} - Openfire running at {}", container.getContainerId(), hostAndPort);
        container.setup();
    }

    @BeforeEach
    public void setUp() {
        auction = new FakeAuctionServer(hostAndPort, "item-54321");
        anotherAuction = new FakeAuctionServer(hostAndPort, "item-65432");
        application = new ApplicationRunner(hostAndPort);
    }

    @Test
    void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        auction.announceClosed();
        application.showsSniperHasLostAuction(auction, 0, 0);
    }

    @Test
    void sniperMakesAHigherBidButLoses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        auction.reportPrice(1000, 98, "other bidder");
        application.hasShownSniperIsBidding(auction, 1000, 1098);
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        auction.announceClosed();
        application.showsSniperHasLostAuction(auction, 1000, 1098);
    }

    @Test
    void sniperWinsAnAuctionByBiddingHigher() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        auction.reportPrice(1000, 98, "other bidder");
        application.hasShownSniperIsBidding(auction, 1000, 1098); // last price, last bid
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_ID);
        application.hasShownSniperIsWinning(auction, 1098); // winning bid
        auction.announceClosed();
        application.showsSniperHasWonAuction(auction, 1098); // last price
    }

    @Test
    void sniperBidsForMultipleItems() throws Exception {
        auction.startSellingItem();
        anotherAuction.startSellingItem();

        application.startBiddingIn(auction, anotherAuction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        anotherAuction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);

        auction.reportPrice(1000, 98, "other bidder");
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID_REGEX);

        anotherAuction.reportPrice(500, 21, "other bidder");
        anotherAuction.hasReceivedBid(521, ApplicationRunner.SNIPER_XMPP_ID_REGEX);

        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_ID);
        anotherAuction.reportPrice(521, 22, ApplicationRunner.SNIPER_ID);

        application.hasShownSniperIsWinning(auction, 1098);
        application.hasShownSniperIsWinning(anotherAuction, 521);

        auction.announceClosed();
        anotherAuction.announceClosed();

        application.showsSniperHasWonAuction(auction, 1098);
        application.showsSniperHasWonAuction(anotherAuction, 521);
    }

    @Test
    void sniperLosesAuctionWhenThePriceIsTooHigh() throws Exception {
        auction.startSellingItem();
        application.startBiddingWithStopPrice(1100, auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        auction.reportPrice(1000, 98, "other bidder");
        application.hasShownSniperIsBidding(auction, 1000, 1098); // last price, last bid

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID_REGEX);

        auction.reportPrice(1197, 10, "third party");
        application.hasShownSniperIsLosing(auction, 1197, 1098);

        auction.reportPrice(1207, 10, "fourth party");
        application.hasShownSniperIsLosing(auction, 1207, 1098);

        auction.announceClosed();
        application.showsSniperHasLostAuction(auction, 1207, 1098);
    }

    @Test
    void sniperReportsInvalidAuctionMessageAndStopsRespondingToEvents() throws Exception {
        String brokenMessage = "a broken message";
        auction.startSellingItem();
        anotherAuction.startSellingItem();

        application.startBiddingIn(auction, anotherAuction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);

        auction.reportPrice(500, 20, "other bidder");
        auction.hasReceivedBid(520, ApplicationRunner.SNIPER_XMPP_ID_REGEX);

        auction.sendInvalidMessageContaining(brokenMessage);
        application.showsSniperHasFailed(auction);

        auction.reportPrice(520, 21, "other bidder");
        waitForAnotherAuctionEvent();

        application.reportsInvalidMessage(auction, brokenMessage);
        application.showsSniperHasFailed(auction);
    }

    private void waitForAnotherAuctionEvent() throws Exception {
        anotherAuction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID_REGEX);
        anotherAuction.reportPrice(600, 6, "other bidder");
        application.hasShownSniperIsBidding(anotherAuction, 600, 606);
    }

    @AfterEach
    void tearDown() {
        auction.stop();
        anotherAuction.stop();
        application.stop();
    }
}
