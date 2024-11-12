package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class SmoothedMovingAverage extends SingleValueCalculationIndicator {

    private final int length;
    private boolean isFirstValue = true; // Flag for first calculation

    public SmoothedMovingAverage(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public SmoothedMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, valueGetter);
        this.length = length;
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (isFirstValue) {
            isFirstValue = false;
            return value;  // Initialize SMMA with the first value
        }
        // Apply the SMMA formula for subsequent values
        return (previousValue * (length - 1) + value) / length;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[] {};
    }
}
