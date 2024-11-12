package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmoothedMovingAverageTest {

    private SmoothedMovingAverage smma;
    private final int period = 5; // SMMA period length

    @BeforeEach
    void setUp() {
        smma = new SmoothedMovingAverage(period, TimeInterval.minutes(1));
    }

    @Test
    void testSmoothedMovingAverageCalculation() {
        // Sample closing prices to test the SMMA calculation
        double[] closingPrices = {10, 12, 11, 13, 12, 14, 15, 13, 14, 16};

        // Expected SMMA values calculated manually for this dataset
        double[] expectedSmmaValues = {10, 10.4, 10.52, 11.016, 11.2128, 11.77024, 12.416192, 12.5339136, 12.82713088, 13.461704704};

        // Test each SMMA value calculated after adding each closing price
        for (int i = 0; i < closingPrices.length; i++) {
            Candle candle = createCandle(closingPrices[i]);
            smma.accumulate(candle);

            System.out.println("Index " + i + ": Expected = " + expectedSmmaValues[i] + ", Actual = " + smma.getValue());

            // Validate SMMA value with expected result
            assertEquals(expectedSmmaValues[i], smma.getValue(), 0.001, "SMMA calculation failed at index " + i);
        }
    }

    // Utility method to create a Candle instance with a specific closing price
    private Candle createCandle(double closingPrice) {
        long start = Instant.now().toEpochMilli();
        long end = start + 60000; // Add 1 minute in milliseconds

        return new Candle(start, end, closingPrice, closingPrice, closingPrice, closingPrice, 1000);
    }
}
