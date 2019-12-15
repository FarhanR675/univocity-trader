package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.simulation.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.account.Allocation.*;
import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

public class AccountManager implements ClientAccount, SimulatedAccountConfiguration {
	private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
	private static final OrderManager DEFAULT_ORDER_MANAGER = new DefaultOrderManager();

	private final Set<String> supportedSymbols = new TreeSet<>();
	private Map<String, String[]> tradedPairs = new ConcurrentHashMap<>();
	private final Map<String, Allocation> allocations = new ConcurrentHashMap<>();
	private final Map<String, Order> pendingOrders = new ConcurrentHashMap<>();
	private final Set<String> lockedPairs = ConcurrentHashMap.newKeySet();

	private static final long BALANCE_EXPIRATION_TIME = minutes(10).ms;
	private static final long FREQUENT_BALANCE_UPDATE_INTERVAL = seconds(15).ms;

	private long lastBalanceSync = 0L;
	private final Map<String, Balance> balances = new ConcurrentHashMap<>();

	private final ClientAccount account;
	private final Map<String, TradingManager> allTradingManagers = new ConcurrentHashMap<>();
	private String referenceCurrencySymbol;
	private final Map<String, OrderManager> orderManagers = new ConcurrentHashMap<>();

	public AccountManager(String referenceCurrencySymbol, ClientAccount account) {
		this.account = account;
		this.referenceCurrencySymbol = referenceCurrencySymbol;
	}

	public void setTradedPairs(Collection<String[]> symbols) {
		tradedPairs.clear();
		symbols.forEach(p -> {
			tradedPairs.put(p[0] + p[1], p);
			supportedSymbols.add(p[0]);
			supportedSymbols.add(p[1]);
		});
	}

	public Map<String, Balance> getBalances() {
		long now = System.currentTimeMillis();
		if ((now - lastBalanceSync) > BALANCE_EXPIRATION_TIME) {
			lastBalanceSync = now;
			updateBalances();
		}
		return balances;
	}

	@Override
	public double getAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getFreeAmount();
	}

	public Balance getBalance(String symbol) {
		return balances.computeIfAbsent(symbol, Balance::new);
	}

	public void subtractFromFreeBalance(String symbol, final BigDecimal amount) {
		Balance balance = getBalance(symbol);
		BigDecimal result = round(balance.getFree().subtract(amount));
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			if (result.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Can't subtract " + amount + " from " + symbol + "'s current free balance of: " + balance.getFree() + ". Insufficient funds.");
			} else {
				result = BigDecimal.ZERO;
			}
		}
		balance.setFree(result);
	}

	public void subtractFromLockedBalance(String symbol, final BigDecimal amount) {
		Balance balance = getBalance(symbol);
		BigDecimal result = round(balance.getLocked().subtract(amount));
		if (result.compareTo(BigDecimal.ZERO) < 0) {
			if (result.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Can't subtract " + amount + " from " + symbol + "'s current locked balance of: " + balance.getLocked() + ". Insufficient funds.");
			} else {
				result = BigDecimal.ZERO;
			}
		}
		balance.setLocked(result);
	}

	private void addToLockedBalance(String symbol, BigDecimal amount) {
		Balance balance = balances.get(symbol);
		if (balance == null) {
			throw new IllegalStateException("Can't lock " + amount + " " + symbol + ". No balance available.");
		}
		amount = balance.getLocked().add(amount);
		balance.setLocked(amount);
	}


	public void addToFreeBalance(String symbol, BigDecimal amount) {
		Balance balance = getBalance(symbol);
		amount = balance.getFree().add(amount);
		balance.setFree(amount);
	}

	@Override
	public synchronized AccountManager setAmount(String symbol, double amount) {
		if (supportedSymbols.contains(symbol) || symbol.equals(referenceCurrencySymbol)) {
			balances.put(symbol, new Balance(symbol, amount));
			return this;
		}
		throw reportUnknownSymbol("Can't set funds", symbol);
	}

	public synchronized AccountManager lockAmount(String symbol, BigDecimal amount) {
		if (supportedSymbols.contains(symbol) || symbol.equals(referenceCurrencySymbol)) {
			subtractFromFreeBalance(symbol, amount);
			addToLockedBalance(symbol, amount);
			return this;
		}
		throw reportUnknownSymbol("Can't set funds", symbol);
	}

	private IllegalArgumentException reportUnknownSymbol(String message, String symbol) {
		throw new IllegalArgumentException(message + ". Account is not managing '" + symbol + "'. Allowed symbols are: " + supportedSymbols + " and " + referenceCurrencySymbol);
	}

	public double allocateFunds(String assetSymbol, String fundSymbol) {
		TradingManager tradingManager = getTradingManagerOf(assetSymbol + fundSymbol);
		if (tradingManager == null) {
			Trader trader = getTraderOf(assetSymbol + referenceCurrencySymbol);
			if (trader != null) {
				tradingManager = trader.tradingManager;
				//fundSymbol = tradingManager.getFundSymbol();
			} else {
				throw new IllegalStateException("Unable to allocate funds to buy " + assetSymbol + ". Unknown symbol: " + assetSymbol);
			}
		}

		double minimumInvestment = allocations.getOrDefault(assetSymbol, NO_LIMITS).getMinimumAmountPerTrade();
		double percentage = allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumPercentagePerAsset() / 100.0;
		double maxAmount = allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumAmountPerAsset();

		double percentagePerTrade = allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumPercentagePerTrade() / 100.0;
		double maxAmountPerTrade = allocations.getOrDefault(assetSymbol, NO_LIMITS).getMaximumAmountPerTrade();

		if (percentage == 0.0 || maxAmount == 0.0) {
			return 0.0;
		}

		double totalFunds = getTotalFundsIn(fundSymbol);
		maxAmountPerTrade = Math.min(totalFunds * percentagePerTrade, maxAmountPerTrade);


		double allocated = getAmount(assetSymbol);
		double unitPrice = tradingManager.getLatestPrice();
		allocated = allocated * unitPrice;

		double available = totalFunds - allocated;
		double allocation = totalFunds * percentage;
		if (allocation > maxAmount) {
			allocation = maxAmount;
		}
		double max = allocation - allocated;
		if (available > max) {
			available = max;
		}
		available = Math.min(maxAmountPerTrade, Math.min(maxAmount, available));

		final double freeAmount = getAmount(fundSymbol);
		double out = Math.min(available, freeAmount);
		if (out < minimumInvestment) {
			out = 0.0;
		}

		return out;
	}

	public double allocateFunds(String assetSymbol) {
		return allocateFunds(assetSymbol, getReferenceCurrencySymbol());
	}

	public synchronized AccountConfiguration maximumInvestmentPercentagePerAsset(double percentage, String... symbols) {
		return updateAllocation("percentage of account", percentage, symbols, (allocation) -> allocation.setMaximumPercentagePerAsset(percentage));
	}


	public synchronized AccountConfiguration maximumInvestmentAmountPerAsset(double maximumAmount, String... symbols) {
		return updateAllocation("maximum expenditure of account", maximumAmount, symbols, (allocation) -> allocation.setMaximumAmountPerAsset(maximumAmount));
	}

	public synchronized AccountConfiguration maximumInvestmentPercentagePerTrade(double percentage, String... symbols) {
		return updateAllocation("percentage of account per trade", percentage, symbols, (allocation) -> allocation.setMaximumPercentagePerTrade(percentage));
	}

	public synchronized AccountConfiguration maximumInvestmentAmountPerTrade(double maximumAmount, String... symbols) {
		return updateAllocation("maximum expenditure per trade", maximumAmount, symbols, (allocation) -> allocation.setMaximumAmountPerTrade(maximumAmount));
	}

	public synchronized AccountConfiguration minimumInvestmentAmountPerTrade(double minimumAmount, String... symbols) {
		return updateAllocation("minimum expenditure per trade", minimumAmount, symbols, (allocation) -> allocation.setMinimumAmountPerTrade(minimumAmount));
	}

	private AccountConfiguration updateAllocation(String description, double param, String[] symbols, Function<Allocation, Allocation> f) {
		if (symbols.length == 0) {
			symbols = supportedSymbols.toArray(new String[0]);
		}
		for (String symbol : symbols) {
			if (supportedSymbols.contains(symbol)) {
				allocations.compute(symbol, (s, allocation) -> allocation == null ? f.apply(new Allocation()) : f.apply(allocation));
			} else {
				reportUnknownSymbol("Can't allocate " + description + " for '" + symbol + "' to " + param, symbol);
			}
		}
		return this;
	}

	public synchronized double getTotalFundsInReferenceCurrency() {
		return getTotalFundsIn(referenceCurrencySymbol);
	}

	public synchronized double getTotalFundsIn(String currency) {
		double total = 0.0;
		Map<String, Double> allPrices = allTradingManagers.values().iterator().next().getAllPrices();
		Map<String, Balance> positions = balances;
		for (var e : positions.entrySet()) {
			String symbol = e.getKey();
			double quantity = e.getValue().getTotal().doubleValue();

			if (currency.equals(symbol)) {
				total += quantity;
			} else {
				double price = allPrices.getOrDefault(symbol + currency, -1.0);
				if (price > 0.0) {
					total += quantity * price;
				} else {
					price = allPrices.getOrDefault(currency + symbol, -1.0);
					if (price > 0.0) {
						total += quantity / price;
					}
				}
			}
		}
		return total;
	}

	public boolean waitingForFill(String assetSymbol, Order.Side side) {
		for (var order : pendingOrders.values()) {
			if (order.getAssetsSymbol().equals(assetSymbol)) {
				if (order.getSide() == side) {
					return true;
				}
			} else if (order.getFundsSymbol().equals(assetSymbol)) {
				// If we want to know if there is an open order to buy BTC,
				// and the symbol is "ADABTC", we need to invert the side as
				// we are selling ADA to buy BTC.
				if (side == BUY) {
					if (order.getSide() == SELL) {
						return true;
					}
				} else if (side == SELL) {
					if (order.getSide() == BUY) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isBuyLocked(String assetSymbol) {
		synchronized (lockedPairs) {
			if (lockedPairs.contains(assetSymbol)) {
				return true;
			}
			if (waitingForFill(assetSymbol, BUY)) {
				return true;
			}
			return false;
		}
	}

	private void lockBuy(String assetSymbol) {
		synchronized (lockedPairs) {
			log.trace("Locking purchases of {}", assetSymbol);
			lockedPairs.add(assetSymbol);
		}
	}

	private void unlockBuy(String assetSymbol) {
		synchronized (lockedPairs) {
			if (lockedPairs.contains(assetSymbol)) {
				log.trace("Unlocking purchases of {}", assetSymbol);
				lockedPairs.remove(assetSymbol);
			}
		}
	}


	@Override
	public synchronized String toString() {
		StringBuilder out = new StringBuilder();
		Map<String, Balance> positions = account.updateBalances();
		positions.entrySet().stream()
				.filter((e) -> e.getValue().getTotal().doubleValue() > 0.00001)
				.forEach((e) -> out
						.append(e.getKey())
						.append(" = $")
						.append(SymbolPriceDetails.toBigDecimal(2, e.getValue().getTotal().doubleValue()).toPlainString())
						.append('\n'));

		return out.toString();
	}

	@Override
	public synchronized Map<String, Balance> updateBalances() {
		long now = System.currentTimeMillis();
		if (now - lastBalanceSync < FREQUENT_BALANCE_UPDATE_INTERVAL) {
			return balances;
		}

		Map<String, Balance> updatedBalances = account.updateBalances();
		if (updatedBalances != null && updatedBalances != balances) {
			updatedBalances.keySet().retainAll(supportedSymbols);
			this.balances.clear();
			this.balances.putAll(updatedBalances);

			updatedBalances.values().removeIf(b -> b.getTotal().compareTo(BigDecimal.ZERO) == 0);
			log.debug("Balances updated: " + updatedBalances);

			lastBalanceSync = System.currentTimeMillis();
		}
		return balances;
	}

	public String getReferenceCurrencySymbol() {
		return referenceCurrencySymbol;
	}

	public Order buy(String assetSymbol, String fundSymbol, double quantity) {
		synchronized (lockedPairs) {
			if (!isBuyLocked(assetSymbol)) {
				try {
					lockBuy(assetSymbol);
					String symbol = assetSymbol + fundSymbol;
					TradingManager tradingManager = getTradingManagerOf(symbol);
					if (tradingManager == null) {
						throw new IllegalStateException("Unable to buy " + quantity + " units of unknown symbol: " + symbol);
					}
					double maxSpend = allocateFunds(assetSymbol);
					if (maxSpend > 0) {
						maxSpend = getTradingFees().takeFee(maxSpend, Order.Type.MARKET, BUY);
						double expectedCost = quantity * tradingManager.getLatestPrice();
						if (expectedCost > maxSpend) {
							quantity = quantity * (maxSpend / expectedCost);
						}
						quantity = quantity * 0.9999;
						OrderRequest orderPreparation = prepareOrder(tradingManager, BUY, quantity);
						return executeOrder(orderPreparation);
					}
				} finally {
					unlockBuy(assetSymbol);
				}
			}
			return null;
		}
	}

	public Order sell(String assetSymbol, String fundSymbol, double quantity) {
		String symbol = assetSymbol + fundSymbol;
		TradingManager tradingManager = getTradingManagerOf(symbol);
		if (tradingManager == null) {
			throw new IllegalStateException("Unable to sell " + quantity + " units of unknown symbol: " + symbol);
		}
		OrderRequest orderPreparation = prepareOrder(tradingManager, SELL, quantity);
		return executeOrder(orderPreparation);
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		if (orderDetails != null) {
			Order order = account.executeOrder(orderDetails);
			if (order != null) {
				switch (order.getStatus()) {
					case NEW:
					case PARTIALLY_FILLED:
						logOrderStatus("Tracking pending order. ", order);
						waitForFill(order);
						return order;
					case FILLED:
						logOrderStatus("Completed order. ", order);
						orderFinalized(null, order);
						return order;
					case CANCELLED:
						logOrderStatus("Could not create order. ", order);
						orderFinalized(null, order);
						return null;
				}
			}
		}
		return null;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	private OrderRequest prepareOrder(TradingManager tradingManager, Order.Side side, double quantity) {
		SymbolPriceDetails priceDetails = tradingManager.getPriceDetails();
		long time = tradingManager.getLatestCandle().closeTime;
		OrderRequest orderPreparation = new OrderRequest(tradingManager.getAssetSymbol(), tradingManager.getFundSymbol(), side, time);
		orderPreparation.setPrice(priceDetails.priceToBigDecimal(tradingManager.getLatestPrice()));
		orderPreparation.setQuantity(priceDetails.adjustQuantityScale(quantity));

		OrderBook book = account.getOrderBook(tradingManager.getSymbol(), 0);


		OrderManager orderCreator = orderManagers.getOrDefault(tradingManager.getSymbol(), DEFAULT_ORDER_MANAGER);
		if (orderCreator != null) {
			orderCreator.prepareOrder(priceDetails, book, orderPreparation, tradingManager.getLatestCandle());
		}

		if (!orderPreparation.isCancelled() && orderPreparation.getTotalOrderAmount().compareTo(priceDetails.getMinimumOrderAmount(orderPreparation.getPrice())) > 0) {
			orderPreparation.setPrice(priceDetails.adjustPriceScale(orderPreparation.getPrice()));
			orderPreparation.setQuantity(priceDetails.adjustQuantityScale(orderPreparation.getQuantity()));

			if (orderPreparation.getTotalOrderAmount().compareTo(priceDetails.getMinimumOrderAmount(orderPreparation.getPrice())) > 0) {
				return orderPreparation;
			}
		}

		return null;
	}

	TradingManager getTradingManagerOf(String symbol) {
		return allTradingManagers.get(symbol);
	}

	public Trader getTraderOf(String symbol) {
		TradingManager tradingManager = getTradingManagerOf(symbol);
		if (tradingManager == null) {
			return null;
		}
		return tradingManager.trader;
	}

	void register(TradingManager tradingManager) {
		this.allTradingManagers.put(tradingManager.getSymbol(), tradingManager);
	}

	public Trader getTraderOfSymbol(String symbol) {
		TradingManager a = allTradingManagers.get(symbol);
		if (a != null) {
			return a.trader;
		}
		return null;
	}

	public Collection<TradingManager> getAllTradingManagers() {
		return allTradingManagers.values();
	}

	public Collection<String[]> getTradedPairs() {
		return tradedPairs.values();
	}

	@Override
	public TradingFees getTradingFees() {
		return account.getTradingFees();
	}

	public AccountConfiguration setOrderManager(OrderManager orderManager, String... symbols) {
		if (symbols.length == 0) {
			symbols = tradedPairs.keySet().toArray(new String[0]);
		}
		for (String symbol : symbols) {
			this.orderManagers.put(symbol, orderManager);
		}
		return this;
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return account.updateOrderStatus(order);
	}

	@Override
	public void cancel(Order order) {
		account.cancel(order);
	}

	private static void logOrderStatus(String msg, Order order) {
		if (log.isTraceEnabled()) {
			//e.g. PARTIALLY_FILLED LIMIT BUY of 1 BTC @ 9000 USDT each after 10 seconds.
			log.trace("{}{} {} {} of {}/{} {} @ {} {} each after {}. Order id: {}, order quantity: {}, amount: ${} of expected ${} {}",
					msg,
					order.getStatus(),
					order.getType(),
					order.getSide(),
					order.getQuantity(),
					order.getExecutedQuantity().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getAssetsSymbol(),
					order.getPrice().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getFundsSymbol(),
					TimeInterval.getFormattedDuration(System.currentTimeMillis() - order.getTime()),
					order.getOrderId(),
					order.getQuantity().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getTotalTraded().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getTotalOrderAmount().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getFundsSymbol());
		}
	}

	@Override
	public boolean isSimulated() {
		return account.isSimulated();
	}

	private void waitForFill(Order order) {
		pendingOrders.put(order.getOrderId(), order);
		if (isSimulated()) {
			return;
		}
		new Thread(() -> {
			Thread.currentThread().setName("Order " + order.getOrderId() + " monitor:" + order.getSide() + " " + order.getSymbol());
			OrderManager orderManager = orderManagers.getOrDefault(order.getSymbol(), DEFAULT_ORDER_MANAGER);
			while (true) {
				try {
					try {
						Thread.sleep(orderManager.getOrderUpdateFrequency().ms);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					Order updated = updateOrder(order);
					if (updated.isFinalized()) {
						return;
					}
				} catch (Exception e) {
					log.error("Error tracking state of order " + order, e);
					return;
				}
			}
		}).start();
	}

	public Order updateOrder(Order order) {
		OrderManager orderManager = orderManagers.getOrDefault(order.getSymbol(), DEFAULT_ORDER_MANAGER);
		Order old = order;
		order = account.updateOrderStatus(order);

		Order.Status s = order.getStatus();
		if (order.isFinalized()) {
			logOrderStatus("", order);
			pendingOrders.remove(order.getOrderId());
			orderFinalized(orderManager, order);
			return order;
		} else { // update order status
			pendingOrders.put(order.getOrderId(), order);
		}

		if (old.getExecutedQuantity().compareTo(order.getExecutedQuantity()) != 0) {
			logOrderStatus("", order);
			updateBalances();
			orderManager.updated(order, traderOf(order));
		} else {
			logOrderStatus("Unchanged ", order);
			orderManager.unchanged(order, traderOf(order));
		}

		//order manager could have cancelled the order
		if (order.getStatus() == CANCELLED) {
			cancelOrder(orderManager, order);
		}
		return order;
	}

	private void orderFinalized(OrderManager orderManager, Order order) {
		orderManager = orderManager == null ? orderManagers.getOrDefault(order.getSymbol(), DEFAULT_ORDER_MANAGER) : orderManager;
		try {
			updateBalances();
		} finally {
			try {
				orderManager.finalized(order, traderOf(order));
			} finally {
				getTradingManagerOf(order.getSymbol()).notifyOrderFinalized(order);
			}
		}
	}

	private Trader traderOf(Order order){
		return getTraderOfSymbol(order.getSymbol());
	}

	private void cancelOrder(OrderManager orderManager, Order order) {
		account.cancel(order);
		order = account.updateOrderStatus(order);
		pendingOrders.remove(order.getOrderId());
		orderManager.finalized(order, traderOf(order));
		logOrderStatus("Cancellation via order manager: ", order);
		updateBalances();
	}


	public synchronized void cancelStaleOrdersFor(Trader trader) {
		if (pendingOrders.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Order> entry : pendingOrders.entrySet()) {
			Order order = entry.getValue();
			OrderManager orderManager = orderManagers.getOrDefault(order.getSymbol(), DEFAULT_ORDER_MANAGER);
			if (orderManager.cancelToReleaseFundsFor(order, traderOf(order), trader)) {
				order.cancel();
				if (order.getStatus() == CANCELLED) {
					cancelOrder(orderManager, order);
					return;
				}
			}
		}
	}

	public SimulatedAccountConfiguration resetBalances() {
		this.balances.clear();
		updateBalances();
		return this;
	}

	public boolean updateOpenOrders(String symbol, Candle candle) {
		if (this.account.updateOpenOrders(symbol, candle)) {
			for (Order order : this.pendingOrders.values()) {
				if (symbol.equals(order.getSymbol())) {
					updateOrder(order);
				}
			}
			return true;
		}
		return false;
	}
}
