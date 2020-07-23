package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.account.Order;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;
import com.univocity.trader.account.Trade;
import com.univocity.trader.account.*;
import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.domain.*;
import com.univocity.trader.exchange.binance.api.client.domain.account.Account;
import com.univocity.trader.exchange.binance.api.client.domain.account.*;
import com.univocity.trader.exchange.binance.api.client.domain.account.request.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.exchange.binance.api.client.exception.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import org.asynchttpclient.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.exchange.binance.api.client.domain.OrderType.*;
import static com.univocity.trader.exchange.binance.api.client.domain.TimeInForce.*;
import static com.univocity.trader.exchange.binance.api.client.domain.account.NewOrder.*;

class BinanceClientAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(BinanceApiRestClient.class);

	private static final AtomicLong id = new AtomicLong(0);
	private final BinanceApiClientFactory factory;
	private final BinanceApiRestClient client;
	private SymbolPriceDetails symbolPriceDetails;
	private BinanceExchange exchangeApi;
	private double minimumBnbAmountToKeep = 1.0;

	public BinanceClientAccount(String apiKey, String secret, BinanceExchange exchangeApi) {
		this.exchangeApi = exchangeApi;

		final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
		final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);

		factory = BinanceApiClientFactory.newInstance(apiKey, secret, asyncHttpClient);
		client = factory.newRestClient();
		new KeepAliveUserDataStream(client).start();
	}

	public double getMinimumBnbAmountToKeep() {
		return minimumBnbAmountToKeep;
	}

	public void setMinimumBnbAmountToKeep(double minimumBnbAmountToKeep) {
		this.minimumBnbAmountToKeep = minimumBnbAmountToKeep;
	}

	private SymbolPriceDetails getPriceDetails() {
		if (symbolPriceDetails == null) {
			symbolPriceDetails = new SymbolPriceDetails(exchangeApi);
		}
		return symbolPriceDetails;
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		String symbol = orderDetails.getSymbol();
		String price = roundStr(orderDetails.getPrice());
		String stopPrice = null;
		Order order = null;
		List<OrderRequest> attachments = orderDetails.attachedOrderRequests();

		if (attachments != null) {
				for (OrderRequest attachment : attachments) {
					if (attachment.getTriggerCondition() == Order.TriggerCondition.STOP_LOSS){
						stopPrice = roundStr(attachment.getPrice());
					} else {
						price = roundStr(attachment.getPrice());
					}

				}
			}


		String finalPrice = price;
		String finalStopPrice = stopPrice;


		switch (orderDetails.getSide()) {
			case BUY:
				switch (orderDetails.getType()) {
					case LIMIT:

						if (stopPrice != null){

							order = execute(orderDetails, q -> limitOCOBuy(symbol, GTC, q, finalStopPrice, finalPrice, finalPrice));
						} else {
							order = execute(orderDetails, q -> limitBuy(symbol, GTC, q, finalPrice));
						}
						break;
					case MARKET:
						return execute(orderDetails, q -> marketBuy(symbol, q));
				}
				break;
			case SELL:
				switch (orderDetails.getType()) {
					case LIMIT:
						if (stopPrice != null){
							order = execute(orderDetails, q -> limitOCOSell(symbol, GTC, q, finalPrice, finalStopPrice, finalStopPrice));
						} else {
							order = execute(orderDetails, q -> limitSell(symbol, GTC, q, finalPrice));
						}


						break;
					case MARKET:
						return execute(orderDetails, q -> marketSell(symbol, q));
				}
		}


		return order ;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		com.univocity.trader.exchange.binance.api.client.domain.market.OrderBook book = client.getOrderBook(symbol, depth == 0 ? 5 : depth);

		OrderBook out = new OrderBook(this, symbol, depth);
		for (OrderBookEntry bid : book.getBids()) {
			out.addBid(Double.parseDouble(bid.getPrice()), Double.parseDouble(bid.getQty()));
		}
		for (OrderBookEntry ask : book.getAsks()) {
			out.addAsk(Double.parseDouble(ask.getPrice()), Double.parseDouble(ask.getQty()));
		}
		return out;
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances(boolean force) {
		Account account = client.getAccount();
		List<AssetBalance> balances = account.getBalances();

		ConcurrentHashMap<String, Balance> out = new ConcurrentHashMap<>();
		for (AssetBalance b : balances) {
			String symbol = b.getAsset();
			Balance balance = new Balance(null, symbol);
			balance.setFree(Double.parseDouble(b.getFree()));
			balance.setLocked(Double.parseDouble(b.getLocked()));
			out.put(symbol, balance);
		}
		return out;
	}

	private Order translate(OrderRequest preparation, OrderDetails response) {
		Order out = new Order(id.incrementAndGet(), preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(response.getSide()), Trade.Side.LONG, response.getTime());

		out.setPrice(response.getAttachments().isEmpty() ?  Double.parseDouble(response.getPrice()) : preparation.getPrice());
		out.setAveragePrice(response.getAttachments().isEmpty() ?  Double.parseDouble(response.getPrice()) : preparation.getPrice());
		out.setQuantity(Double.parseDouble(response.getOrigQty()));
		out.setExecutedQuantity(Double.parseDouble(response.getExecutedQty()));
		out.setOrderId(String.valueOf(response.getOrderId()));
		out.setStatus(translate(response.getStatus()));
		out.setType(translate(response.getType()));

		for (OrderDetails orderDetails: response.getAttachments()){
			Order child = new Order(id.incrementAndGet(), preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(orderDetails.getSide()), Trade.Side.LONG, orderDetails.getTime());

			child.setTriggerCondition(orderDetails.getType() == STOP_LOSS_LIMIT ? Order.TriggerCondition.STOP_LOSS : Order.TriggerCondition.STOP_GAIN, Double.parseDouble(orderDetails.getPrice()));
			child.setPrice(Double.parseDouble(orderDetails.getPrice()));
			child.setAveragePrice(Double.parseDouble(orderDetails.getPrice()));
			child.setQuantity(Double.parseDouble(orderDetails.getOrigQty()));
			child.setExecutedQuantity(Double.parseDouble(orderDetails.getExecutedQty()));
			child.setOrderId(String.valueOf(orderDetails.getOrderId()));
			child.setStatus(translate(orderDetails.getStatus()));
			child.setType(translate(orderDetails.getType()));
			child.setParent(out);
		}
		return out;
	}

	private Order.Side translate(OrderSide side) {
		switch (side) {
			case BUY:
				return Order.Side.BUY;
			case SELL:
				return SELL;
		}
		throw new IllegalStateException("Can't translate " + side + " to Order.Side");
	}

	private Order.Status translate(OrderStatus status) {
		switch (status) {
			case EXPIRED:
			case CANCELED:
			case PENDING_CANCEL:
			case REJECTED:
				return Order.Status.CANCELLED;

			case NEW:
				return Order.Status.NEW;
			case FILLED:
				return FILLED;
			case PARTIALLY_FILLED:
				return Order.Status.PARTIALLY_FILLED;
		}
		throw new IllegalStateException("Can't translate " + status + " to Order.Status");
	}

	private Order.Type translate(OrderType type) {
		switch (type) {
			case LIMIT:
			case LIMIT_MAKER:
			case STOP_LOSS_LIMIT:
				return Order.Type.LIMIT;
			case MARKET:
				return Order.Type.MARKET;

		}
		throw new IllegalStateException("Can't translate " + type + " to Order.Type");
	}

	private Order execute(OrderRequest orderPreparation, Function<String, NewOrder> orderFunction) {
		if (orderPreparation.getSide() == SELL && orderPreparation.getAssetsSymbol().equalsIgnoreCase("BNB")) {
			double newQuantity = orderPreparation.getQuantity() - minimumBnbAmountToKeep;
			orderPreparation.setQuantity(newQuantity);
		}

		SymbolPriceDetails f = getPriceDetails().switchToSymbol(orderPreparation.getSymbol());
		if (orderPreparation.getTotalOrderAmount()> f.getMinimumOrderAmount(orderPreparation.getPrice())) {
			NewOrder order = null;
			try {
				BigDecimal qty = f.adjustQuantityScale(orderPreparation.getQuantity());
				order = orderFunction.apply(qty.toPlainString());
				log.info("Executing {} order: {}", order.getType(), order);

				return translate(orderPreparation, order.getStopPrice() != null ? client.newOrderOCO(order.newOrderRespType(NewOrderResponseType.FULL)) : client.newOrder(order.newOrderRespType(NewOrderResponseType.FULL)));
			} catch (BinanceApiException e) {
				log.error("Error processing order " + order, e);
			}
		}
		return null;
	}

	@Override
	public Order updateOrderStatus(Order order) {
		OrderStatusRequest request = new OrderStatusRequest(order.getSymbol(), Long.valueOf(order.getOrderId()));
		return translate(order, client.getOrderStatus(request));
	}

	private Order translate(Order original, com.univocity.trader.exchange.binance.api.client.domain.account.Order order) {
		Order out = new Order(original.getInternalId(), original.getAssetsSymbol(), original.getFundsSymbol(), translate(order.getSide()), Trade.Side.LONG, order.getTime());
		out.setStatus(translate(order.getStatus()));
		out.setExecutedQuantity(Double.parseDouble(order.getExecutedQty()));
		out.setAveragePrice(Double.parseDouble(order.getPrice()));
		out.setPrice(Double.parseDouble(order.getPrice()));
		out.setOrderId(String.valueOf(order.getOrderId()));
		out.setType(translate(order.getType()));
		out.setQuantity(Double.parseDouble(order.getOrigQty()));
		out.setTrade(original.getTrade());
		return out;
	}

	@Override
	public void cancel(Order order) {
		try{
			// Try to fetch existing order status
			com.univocity.trader.exchange.binance.api.client.domain.account.Order status = client.getOrderStatus(new OrderStatusRequest(order.getSymbol(), Long.valueOf(order.getOrderId())));
			// Order might have been cancelled manually by user
			if (OrderStatus.CANCELED.equals(status.getStatus()) || OrderStatus.PENDING_CANCEL.equals(status.getStatus())){
				log.info("Order {} was already cancelled or is pending cancellation", order);
			} else {
				CancelOrderResponse response = client.cancelOrder(new CancelOrderRequest(order.getSymbol(), Long.valueOf(order.getOrderId())));
				log.info("Cancelled order {}. Response: {}", order, response);
			}
		} catch (BinanceApiException e){
			if (!"Unknown order sent.".equals(e.getMessage())){
				throw e;
			}
			log.debug("Attempted to cancel an order that was not found on Binance (order {})", order);
		}
	}
}
