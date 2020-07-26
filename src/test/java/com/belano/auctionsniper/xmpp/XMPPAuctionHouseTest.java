package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.Auction;
import com.belano.auctionsniper.AuctionEventListener;
import com.belano.auctionsniper.FakeAuctionServer;
import com.belano.auctionsniper.Item;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.belano.auctionsniper.ApplicationRunner.SNIPER_ID;
import static com.belano.auctionsniper.ApplicationRunner.SNIPER_PASSWORD;
import static com.belano.auctionsniper.ApplicationRunner.SNIPER_XMPP_ID_REGEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Testcontainers
@TestInstance(PER_CLASS)
@Tag("integration")
public class XMPPAuctionHouseTest {

    private static final Logger logger = LoggerFactory.getLogger(XMPPAuctionHouseTest.class);

    @Container
    private static final OpenfireContainer container = new OpenfireContainer();
    private static final String ITEM_ID = "item-54321";
    private static HostAndPort hostAndPort;

    private FakeAuctionServer auctionServer;
    private AuctionHouse auctionHouse;

    @BeforeAll
    public void setUpOpenfire() {
        hostAndPort = HostAndPort.fromParts(container.getHost(), container.getMappedPort(5222));
        logger.info(">> Container id: {} - Openfire running at {}", container.getContainerId(), hostAndPort);
        container.setup();
    }

    @BeforeEach
    public void setUp() throws Exception {
        auctionServer = new FakeAuctionServer(hostAndPort, ITEM_ID);
        auctionServer.startSellingItem();
    }

    @AfterEach
    void tearDown() {
        auctionServer.stop();
        auctionHouse.disconnect();
    }

    @Test
    void receivesEventsFromAuctionServerAfterJoining() throws Exception {
        CountDownLatch auctionWasClosed = new CountDownLatch(1);
        auctionHouse = XMPPAuctionHouse.connectTo(
                hostAndPort.getHostText(),
                String.valueOf(hostAndPort.getPort()),
                SNIPER_ID,
                SNIPER_PASSWORD
        );
        Auction auction = auctionHouse.auctionFor(new Item(ITEM_ID, 0));
        auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed));

        auction.join();
        auctionServer.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID_REGEX);
        auctionServer.announceClosed();

        assertThat("Should have been closed", auctionWasClosed.await(2, TimeUnit.SECONDS), is(true));
    }

    private AuctionEventListener auctionClosedListener(CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            @Override
            public void auctionClosed() {
                auctionWasClosed.countDown();
            }

            @Override
            public void currentPrice(int currentPrice, int increment, PriceSource priceSource) {
                // not implemented
            }

            @Override
            public void auctionFailed() {
                // not implemented
            }
        };
    }
}