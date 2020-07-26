package com.belano.auctionsniper;

import org.hamcrest.Matcher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.LogManager;

import static org.hamcrest.MatcherAssert.assertThat;

public class AuctionLogDriver {
    public static final String LOG_FILE_NAME = "auction-sniper.log";
    private final File logFile = new File(LOG_FILE_NAME);

    public void hasEntry(Matcher<String> matcher) throws IOException {
        assertThat(FileUtils.readFileToString(logFile, Charset.defaultCharset()), matcher);
    }

    public void clearLog() {
        if (logFile.delete()) {
            LogManager.getLogManager().reset();
        }
    }
}
