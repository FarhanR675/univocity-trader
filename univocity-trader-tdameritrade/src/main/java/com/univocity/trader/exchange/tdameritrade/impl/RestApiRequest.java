package com.univocity.trader.exchange.tdameritrade.impl;

import okhttp3.Request;

public class RestApiRequest<T> {

  public Request request;
  RestApiJsonParser<T> jsonParser;
}
