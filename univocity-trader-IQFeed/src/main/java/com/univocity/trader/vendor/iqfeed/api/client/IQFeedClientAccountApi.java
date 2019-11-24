package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.ClientAccountApi;
import com.univocity.trader.IQFeedExchangeAPI;
import com.univocity.trader.SymbolPriceDetails;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.*;
import org.asynchttpclient.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IQFeedClientAccountApi {

    private static final Logger log = LoggerFactory.getLogger(IQFeedApiWebSocketClient.class);

    private final IQFeedApiClientFactory factory;
    private final IQFeedApiWebSocketClient client;
    private SymbolPriceDetails symbolPriceDetails;
    private IQFeedExchangeAPI exchangeAPI;
    private double minimumBnbAmountToKeep = 1.0;

    public IQFeedClientAccountApi(String iqPortalPath, String product, String version, String login, String password,
                                  IQFeedExchangeAPI exchangeAPI, boolean autoconnect = False, boolean saveLogin = False){
        this.exchangeAPI = exchangeAPI;
        final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
        final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);
        factory = IQFeedApiClientFactory.newInstance(iqPortalPath, product, version, login, password, autoconnect, saveLogin, asyncHttpClient);
        client = factory.newWebSocketClient();
    }

    public double getMinimumBnbAmountToKeep() { return minimumBnbAmountToKeep;}

    public void setMinimumBnbAmountToKeep(double minimumBnbAmountToKeep){
        this.minimumBnbAmountToKeep = minimumBnbAmountToKeep;
    }

    private SymbolPriceDetails getSymbolPriceDetails(){
        if(symbolPriceDetails == null){
            symbolPriceDetails = new SymbolPriceDetails(exchangeAPI);
        }
        return symbolPriceDetails;
    }

}
