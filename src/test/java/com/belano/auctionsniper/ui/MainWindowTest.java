package com.belano.auctionsniper.ui;

import com.belano.auctionsniper.AuctionSniperDriver;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;

@Tag("integration")
public class MainWindowTest {

    private final SnipersTableModel tableModel = new SnipersTableModel();
    private final MainWindow mainWindow = new MainWindow(tableModel);
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);

    @Test
    void makesUserRequestWhenJoinButtonClicked() {
        final ValueMatcherProbe<String> buttonProbe =
                new ValueMatcherProbe<>(equalTo("an item-id"), "join request");

        mainWindow.addUserRequestListener(buttonProbe::setReceivedValue);

        driver.startBiddingFor("an item-id");
        driver.check(buttonProbe);
    }
}