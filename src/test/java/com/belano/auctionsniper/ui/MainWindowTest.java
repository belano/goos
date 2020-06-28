package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.AuctionSniperDriver;
import com.belano.auctionsniper.Item;
import com.belano.auctionsniper.SniperPortfolio;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;

@Tag("integration")
public class MainWindowTest {

    private final SniperPortfolio portfolio = new SniperPortfolio();
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);
    private MainWindow mainWindow;

    @BeforeEach
    void setUp() {
        mainWindow = new MainWindow(portfolio);
    }

    @Test
    void makesUserRequestWhenJoinButtonClicked() {
        final ValueMatcherProbe<Item> buttonProbe =
                new ValueMatcherProbe<>(equalTo(new Item("an item-id", 789)), "join request");

        mainWindow.addUserRequestListener(buttonProbe::setReceivedValue);

        driver.startBiddingFor("an item-id", 789);
        driver.check(buttonProbe);
    }

    @AfterEach
    void tearDown() {
        mainWindow.dispose();
    }
}