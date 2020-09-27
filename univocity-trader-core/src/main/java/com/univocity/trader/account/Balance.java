package com.univocity.trader.account;

import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.config.Allocation.*;

public final class Balance implements Cloneable {

	private static final Logger log = LoggerFactory.getLogger(Balance.class);
	public static final Balance ZERO = new Balance(null, "");

	private final Map<String, AtomicLong> balanceUpdateCounts;
	private final String symbol;
	private double free = 0.0;
	private double locked = 0.0;
	private double shorted = 0.0;
	private final Map<String, Double> marginReserves = new ConcurrentHashMap<>();
	private String[] shortedAssetSymbols;
	private boolean tradingLocked = false;

	public static final MathContext ROUND_MC_STR = new MathContext(8, RoundingMode.HALF_EVEN);
	public static final MathContext ROUND_MC = new MathContext(12, RoundingMode.HALF_EVEN);

	public Balance(AccountManager accountManager, String symbol) {
		this.balanceUpdateCounts = accountManager != null ? accountManager.balanceUpdateCounts : new ConcurrentHashMap<>();
		this.symbol = symbol;
	}

	public Balance(AccountManager accountManager, String symbol, double free) {
		this.balanceUpdateCounts = accountManager != null ? accountManager.balanceUpdateCounts : new ConcurrentHashMap<>();
		this.symbol = symbol;
		this.free = ensurePositive(free, "free balance");
	}

	public String getSymbol() {
		return symbol;
	}

	public double getFree() {
		return free;
	}

	public Balance setFree(double free) {
		this.free = ensurePositive(free, "free balance");
		return this;
	}

	public double getLocked() {
		return locked;
	}

	public Balance setLocked(double locked) {
		this.locked = ensurePositive(locked, "locked balance");
		return this;
	}

	public double getShorted() {
		return shorted;
	}

	public Balance setShorted(double shorted) {
		this.shorted = ensurePositive(shorted, "shorted balance");
		return this;
	}

	public double getMarginReserve(String assetSymbol) {
		return marginReserves.getOrDefault(assetSymbol, 0.0);
	}

	public String[] getShortedAssetSymbols() {
		if (shortedAssetSymbols == null) {
			shortedAssetSymbols = marginReserves.keySet().toArray(new String[0]);
		}
		return shortedAssetSymbols;
	}

	public Balance setMarginReserve(String assetSymbol, double marginReserve) {
		marginReserve = ensurePositive(marginReserve, "margin reserve");
		if (marginReserve <= 0) {
			this.marginReserves.remove(assetSymbol);
		} else {
			this.marginReserves.put(assetSymbol, marginReserve);
		}
		shortedAssetSymbols = null;
		return this;
	}

	private double ensurePositive(double bd, String field) {
//		String msg = symbol + " " + field + " = " + roundStr(bd);
//		System.out.println(msg);

		//bd = round(bd);
		if (bd >= 0) {
			balanceUpdateCounts.computeIfAbsent(symbol, (s) -> new AtomicLong(1)).incrementAndGet();
			return bd;
		}
		if (bd >= -EFFECTIVELY_ZERO) {
			balanceUpdateCounts.computeIfAbsent(symbol, (s) -> new AtomicLong(1)).incrementAndGet();
			return 0.0;
		} else {
			throw new IllegalStateException(symbol + ": can't set " + field + " to  " + bd);
		}
	}

	public double getTotal() {
		return free + locked;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder(symbol);

		double free = getFree();
		double locked = getLocked();
		double shorted = getShorted();

		out.append('(');
		if (free > 0 || locked > 0 || shorted > 0) {
			out.append(roundStr(free));
			if (locked > 0) {
				out.append(", ").append(roundStr(locked)).append(" locked");
			}
			if (shorted > 0) {
				out.append(", ").append(roundStr(locked)).append(" shorted");
				if (!marginReserves.isEmpty()) {
					out.append(" - margin: {");
					boolean first = true;
					Map<String, Double> sorted = new TreeMap<>(marginReserves);
					for (var entry : sorted.entrySet()) {
						if (first) {
							first = false;
						} else {
							out.append(", ");
						}
						out.append(entry.getKey()).append('=').append(roundStr(entry.getValue()));
					}
					out.append('}');
				}
			}
		} else {
			out.append("0.0");
		}
		out.append(')');
		return out.toString();
	}

	private static final BigDecimal round(BigDecimal bd, MathContext mc) {
		if (bd.scale() != mc.getPrecision()) {
			return bd.setScale(mc.getPrecision(), mc.getRoundingMode());
		}
		return bd;
	}

	public static final String roundStr(double bd) {
		return round(BigDecimal.valueOf(bd), ROUND_MC_STR).toPlainString();
	}

	public static final double round(double bd) {
		return round(BigDecimal.valueOf(bd), ROUND_MC).doubleValue();
	}

	public final boolean isTradingLocked() {
		return tradingLocked;
	}

	public final void lockTrading() {
		if (log.isTraceEnabled()) {
			log.trace("Locking trading on {}", symbol);
		}
		tradingLocked = true;
	}

	public final void unlockTrading() {
		if (log.isTraceEnabled()) {
			log.trace("Unlocking trading on {}", symbol);
		}
		tradingLocked = false;
	}

	public Balance clone() {
		try {
			return (Balance) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

}
