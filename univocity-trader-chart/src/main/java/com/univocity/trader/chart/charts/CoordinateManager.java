package com.univocity.trader.chart.charts;

import com.univocity.trader.chart.charts.painter.*;
import com.univocity.trader.chart.dynamic.*;

public abstract class CoordinateManager {
	protected double maximum = Double.MIN_VALUE;
	protected double minimum = Double.MAX_VALUE;
	private double logLow;
	private double logRange;

	public double getMaximum() {
		return maximum;
	}

	public double getMinimum() {
		return minimum;
	}

	public void updateEdgeValues(boolean logScale, int from, int to) {
		minimum = Integer.MAX_VALUE;
		maximum = Integer.MIN_VALUE;

		updateMinAndMax(logScale, from, to);
		// avoids touching upper and lower limits of the chart
		minimum = minimum * 0.995;
		maximum = maximum * 1.0005;

		if(minimum == 0){
			minimum = 0.0000000000000001;
		}

		updateLogarithmicData();
	}

	protected abstract void updateMinAndMax(boolean logScale, int from, int to);

	private void updateLogarithmicData() {
		logLow = Math.log10(minimum);
		logRange = Math.log10(maximum) - logLow;
	}

	public int getLogarithmicYCoordinate(double value, int height) {
		return height - (int) ((Math.log10(value) - logLow) * height / logRange);
	}

	public int getLinearYCoordinate(double value, int height) {
		double linearRange = height - (height * minimum / maximum);
		double proportion = (height - (height * value / maximum)) / linearRange;

		return (int) (height * proportion);
	}

	public double getValueAtY(int y, int height) {
		if (displayLogarithmicScale()) {
			return Math.pow(10, (y + logLow * height / logRange) / height * logRange);
		} else {
			return minimum + ((maximum - minimum) / height) * y;
		}
	}

	public final int getYCoordinate(Painter.Overlay overlay, double value, int height) {
		return overlay != Painter.Overlay.NONE && displayLogarithmicScale() ? getLogarithmicYCoordinate(value, height) : getLinearYCoordinate(value, height);
	}

	private boolean displayLogarithmicScale() {
		return !(minimum < 0) && theme().isDisplayingLogarithmicScale();
	}

	protected abstract Theme theme();
}
