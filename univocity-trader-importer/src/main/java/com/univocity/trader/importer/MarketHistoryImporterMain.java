package com.univocity.trader.importer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.candles.CandleRepository;
import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;
import com.univocity.trader.currency.Currencies;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.exchange.ExchangeFactory;
import com.univocity.trader.guice.UnivocityFactory;
import com.univocity.trader.indicators.base.TimeInterval;

public class MarketHistoryImporterMain {
   /**
    * configfile option
    */
   private static final String CONFIG_OPTION = "config";
   /**
    * currencies
    */
   private static final String CURRENCIES_OPTION = "currencies";

   public static void main(String... args) {
      System.out.println("Univocity History Loader");
      /*
       * options
       */
      final Options options = new Options();
      Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(true).desc("config file").build();
      options.addOption(oo);
      oo = Option.builder().argName(CURRENCIES_OPTION).longOpt(CURRENCIES_OPTION).type(String.class).hasArg().required(true).desc("currencies").build();
      options.addOption(oo);
      /*
       * parse
       */
      final CommandLineParser parser = new DefaultParser();
      CommandLine cmd = null;
      try {
         cmd = parser.parse(options, args);
         /*
          * get the file
          */
         final String configFileName = cmd.getOptionValue(CONFIG_OPTION);
         /*
          * get the currencies
          */
         final String currenciesList = cmd.getOptionValue(CURRENCIES_OPTION);
         if ((null != configFileName) && (null != currenciesList)) {
            final String[] currencies = Currencies.getInstance().fromList(currenciesList);
            final String[] pairs = Currencies.getInstance().makePairs(currencies, new String[] { "USDT" });
            ConfigFileUnivocityConfigurationImpl.setConfigfileName(configFileName);
            final Exchange<Candle> exchange = ExchangeFactory.getInstance().getExchange(UnivocityFactory.getInstance().getUnivocityConfiguration().getExchangeClass());
            final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
            for (final String symbol : pairs) {
               CandleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1));
            }
         }
      } catch (final Exception e) {
         e.printStackTrace();
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("posix", options);
      }
   }
}
