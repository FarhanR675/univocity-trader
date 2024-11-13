package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class ZigZagIndicator extends SingleValueCalculationIndicator {

    private final double threshold;
    private double lastPeak;
    private double lastTrough;
    private boolean isTrendUp;

    public ZigZagIndicator (TimeInterval interval, double threshold) {
        super(interval, c -> c.close);
        this.threshold = threshold;
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (getAccumulationCount() == 0) {
            lastPeak = value;
            lastTrough = value;
            return value;
        }

        double percentageChangeFromPeak = ((value - lastPeak) / lastPeak) * 100;
        double percentageChangeFromTrough = ((value - lastTrough) / lastTrough) * 100;

        if (isTrendUp) {
            if (percentageChangeFromPeak <= -threshold) {
                lastTrough = value;
                isTrendUp = false;
                return value;
            } else if (value > lastPeak) {
                lastPeak = value;
            }
        } else {
            if (percentageChangeFromTrough >= threshold) {
                lastPeak = value;
                isTrendUp = true;
                return value;
            } else if (value < lastTrough) {
                lastTrough = value;
            }
        }
        return isTrendUp ? lastPeak : lastTrough;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[] {};
    }
}
