package com.univocity.trader.chart.charts.painter;

import com.univocity.trader.chart.charts.*;
import com.univocity.trader.chart.dynamic.*;

import java.awt.*;

public interface Painter<T extends Theme> extends Repaintable {

	Insets NO_INSETS = new Insets(0, 0, 0, 0);

	enum Overlay {
		FRONT, BACK, NONE
	}

	Overlay overlay();

	void paintOn(BasicChart<?> chart, Graphics2D g, int width);

	T theme();

	default Insets insets(){
		return NO_INSETS;
	}

	default Rectangle bounds(){
		return null;
	}

	default void install(BasicChart<?> chart) {

	}

	default void uninstall(BasicChart<?> chart) {

	}

	default double maximumValue(int from, int to) {
		return Integer.MIN_VALUE;
	}

	default double minimumValue(int from, int to) {
		return Integer.MAX_VALUE;
	}

	static Point createCoordinate(BasicChart<?> chart, AreaPainter painter, int candleIndex, double value) {
		Point p = new Point();
		p.x = chart.getXCoordinate(candleIndex);
		if(painter == null || painter.bounds() == null){
			p.y = chart.getYCoordinate(value);
		} else {
			Rectangle bounds = painter.bounds();
			p.y =  chart.getAvailableHeight() +  painter.getYCoordinate(value, bounds.height);
		}

		return p;
	}
}
