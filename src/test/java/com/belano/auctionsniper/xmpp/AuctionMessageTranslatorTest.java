package com.belano.auctionsniper.xmpp;

import com.belano.auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.belano.auctionsniper.AuctionEventListener.PriceSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuctionMessageTranslatorTest {

    private static final Chat UNUSED_CHAT = null;
    public static final String SNIPER_ID = "sniper";

    @Mock
    private AuctionEventListener listener;

    @Mock
    XMPPFailureReporter failureReporter;

    private AuctionMessageTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter);
    }

    @Test
    void notifiesAuctionClosedWhenCloseMessageReceived() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");

        translator.processMessage(UNUSED_CHAT, message);

        verify(listener).auctionClosed();
    }

    @Test
    void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;");

        translator.processMessage(UNUSED_CHAT, message);

        verify(listener).currentPrice(192, 7, PriceSource.FROM_OTHER_BIDDER);
    }

    @Test
    void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";");

        translator.processMessage(UNUSED_CHAT, message);

        verify(listener).currentPrice(234, 5, PriceSource.FROM_SNIPER);
    }

    @Test
    void notifiesAuctionFailedWhenBadMessageReceived() {
        Message message = new Message();
        String badMessage = "a bad message";
        message.setBody(badMessage);

        translator.processMessage(UNUSED_CHAT, message);

        verify(listener).auctionFailed();
        verify(failureReporter).cannotTranslateMessage(
                eq(SNIPER_ID), eq(badMessage), any(Exception.class));
    }

    @Test
    void notifiesAuctionFailedWhenEventTypeMissing() {
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";");

        translator.processMessage(UNUSED_CHAT, message);

        verify(listener).auctionFailed();
    }
}