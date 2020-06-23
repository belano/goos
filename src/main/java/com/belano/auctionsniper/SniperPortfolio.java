package com.belano.auctionsniper;

import java.util.ArrayList;
import java.util.List;

public class SniperPortfolio implements SniperCollector {

    private final List<AuctionSniper> snipers = new ArrayList<>();
    private PortfolioListener portfolioListener;

    @Override
    public void addSniper(AuctionSniper sniper) {
        snipers.add(sniper);
        portfolioListener.sniperAdded(sniper);
    }

    public void addPortfolioListener(PortfolioListener portfolioListener) {
        this.portfolioListener = portfolioListener;
    }
}
