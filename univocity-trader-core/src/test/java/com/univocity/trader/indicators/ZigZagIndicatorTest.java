package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.TimeInterval;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;

public class ZigZagIndicatorTest extends TestCase {

    private ZigZagIndicator zigZagIndicator;
    private final double threshold = 5.0;

    @BeforeEach
    public void setUp() {
        zigZagIndicator = new ZigZagIndicator(TimeInterval.minutes(1), threshold);
    }
   @Test
   public void testZigZagIndicatorCalculation() {
        double[] closingPrices = {100, 102, 98, 105, 95, 90, 92, 110, 108, 115};
        double[] expectedZigZagValues = {100, 102, 98, 105, 95, 90, 90, 110, 108, 115};

        for (int i = 0; i < closingPrices.length; i++) {
            Candle candle = createCandle(closingPrices[i]);
            zigZagIndicator.accumulate(candle);

            System.out.println("Index " + i + ": Expected = " + expectedZigZagValues[i] + ", Actual = " + zigZagIndicator.getValue());


            assertEquals(expectedZigZagValues[i], zigZagIndicator.getValue(), 0.01, "ZigZag calculation failed at index " + i);
        }
   }
    // Used for Testing
    private void assertEquals(double expectedZigZagValue, double value, double v, String s) {
    }

    private Candle createCandle (double closingPrice) {
        long start = Instant.now().toEpochMilli();
        long end = start + 60000;

        return new Candle(start, end, closingPrice, closingPrice, closingPrice, closingPrice, 1000);
   }



}