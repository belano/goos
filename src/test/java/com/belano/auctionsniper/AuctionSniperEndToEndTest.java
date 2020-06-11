package com.belano.auctionsniper;

import com.belano.testcontainers.OpenfireContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HostAndPort;

@Testcontainers
@Tag("integration")
public class AuctionSniperEndToEndTest {

    private static final Logger logger = LoggerFactory.getLogger(AuctionSniperEndToEndTest.class);

    @Container
    private final OpenfireContainer container = new OpenfireContainer();

    private ApplicationRunner application;
    private FakeAuctionServer auction;

    @BeforeEach
    public void setUp() {
        HostAndPort hostAndPort = HostAndPort.fromParts(container.getHost(), container.getMappedPort(5222));
        logger.info(">> Openfire running at {}", hostAndPort);

        auction = new FakeAuctionServer(hostAndPort, "item-54321");
        application = new ApplicationRunner(hostAndPort);

        container.setup();
    }

    @Test
    void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFromSniper();
        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @AfterEach
    void tearDown() {
        auction.stop();
        application.stop();
    }
}
