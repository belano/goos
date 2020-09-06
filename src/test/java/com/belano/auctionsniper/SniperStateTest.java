package com.belano.auctionsniper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SniperStateTest {

    private static Stream<Arguments> provideValidTransitionsWhenAuctionClosed() {
        return Stream.of(
                Arguments.of(SniperState.JOINING, SniperState.LOST),
                Arguments.of(SniperState.BIDDING, SniperState.LOST),
                Arguments.of(SniperState.WINNING, SniperState.WON)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidTransitionsWhenAuctionClosed")
    void validTransitionsWhenAuctionClosed(SniperState currentState, SniperState targetState) {
        assertThat("Unexpected transition", currentState.whenAuctionClosed(), equalTo(targetState));

    }

    @ParameterizedTest
    @EnumSource(value = SniperState.class, names = {"LOST", "WON", "FAILED"})
    void invalidTransitionsWhenAuctionClosed(SniperState state) {
        Defect defect = assertThrows(Defect.class, state::whenAuctionClosed);
        assertThat(defect.getMessage(), is(notNullValue()));
    }
}