package com.belano.auctionsniper.xmpp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoggingXMPPFailureReporterTest {

    @Mock
    private Logger logger;

    private LoggingXMPPFailureReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new LoggingXMPPFailureReporter(logger);
    }

    @AfterEach
    void tearDown() {
        LogManager.getLogManager()
                .reset();
    }

    @Test
    void writesMessageTranslationFailureToLog() {
        reporter.cannotTranslateMessage("auction id", "bad message", new Exception("bad"));

        verify(logger).severe("<auction id> " +
                "Could not translate message \"bad message\" " +
                "because \"java.lang.Exception: bad\"");
    }
}