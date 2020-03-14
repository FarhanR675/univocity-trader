package com.univocity.trader.indicators.base.adx;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MinusDIIndicator extends AbstractDIIndicator {

	public MinusDIIndicator(int length, TimeInterval interval) {
		super(length, interval);
	}

	@Override
	protected AbstractDMIndicator getDMIndicator(TimeInterval interval) {
		return Indicators.MinusDMIndicator(interval);
	}
}
