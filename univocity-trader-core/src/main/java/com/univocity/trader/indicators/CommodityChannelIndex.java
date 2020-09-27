package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class CommodityChannelIndex extends SingleValueIndicator {

    private double value;

    private final double factor;
    private final TypicalPrice typicalPriceInd;
    private final MovingAverage smaInd;
    private final MeanDeviation meanDeviationInd;

    public CommodityChannelIndex(int length, TimeInterval interval) {
        super(interval, null);
        factor = 0.015;
        typicalPriceInd = new TypicalPrice(interval);
        smaInd = new MovingAverage(length, interval, c -> typicalPriceInd.getValue());
        meanDeviationInd = new MeanDeviation(length, interval, c -> typicalPriceInd.getValue());
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        typicalPriceInd.accumulate(candle);
        smaInd.accumulate(candle);
        meanDeviationInd.accumulate(candle);

        final double typicalPrice = typicalPriceInd.getValue();
        final double typicalPriceAvg = smaInd.getValue();
        final double meanDeviation = meanDeviationInd.getValue();

        if (meanDeviation == 0.0) {
            this.value = 0.0;
        } else {
            this.value = (typicalPrice - typicalPriceAvg) / (meanDeviation * factor);
        }

        return true;

    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{typicalPriceInd, smaInd, meanDeviationInd};
    }

}
