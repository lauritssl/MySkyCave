package cloud.cave.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.cave.ipc.*;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.*;

/**
 * Concrete ServerFactory that creates server side delegates based upon dynamic class
 * loading of classes whose qualified names are defined by a set of environment
 * variables, as advised by 12factor.com. After creation, each service delegate
 * is configured through their 'initialize' method with their service end point
 * configuration, again based upon reading their respective environment
 * variable.
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 * 
 */
public class EnvironmentServerFactory implements CaveServerFactory {

  private Logger logger;
  private EnvironmentReaderStrategy environmentReader;

  public EnvironmentServerFactory(EnvironmentReaderStrategy envReader) {
    logger = LoggerFactory.getLogger(EnvironmentServerFactory.class);
    this.environmentReader = envReader;
  }

  @Override
  public CaveStorage createCaveStorage() {
    CaveStorage caveStorage = null; 
    caveStorage = Config.loadAndInstantiate(environmentReader, 
        Config.SKYCAVE_CAVESTORAGE_IMPLEMENTATION, caveStorage);

    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(environmentReader, Config.SKYCAVE_DBSERVER);
    caveStorage.initialize(config);
    
    logger.info("Creating cave storage with cfg: "+ config);
    
    return caveStorage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector() {
    SubscriptionService subscriptionService = null; 
    subscriptionService = Config.loadAndInstantiate(environmentReader, 
        Config.SKYCAVE_SUBSCRIPTION_IMPLEMENTATION, subscriptionService);
    
    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(environmentReader, Config.SKYCAVE_SUBSCRIPTIONSERVER);
    subscriptionService.initialize(config);

    logger.info("Creating subscription service with cfg: "+ config);

    return subscriptionService;
  }

  @Override
  public WeatherService createWeatherServiceConnector() {
    WeatherService weatherService = null; 
    weatherService = Config.loadAndInstantiate(environmentReader, 
        Config.SKYCAVE_WEATHER_IMPLEMENATION, weatherService);
    
    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(environmentReader, Config.SKYCAVE_WEATHERSERVER);
    weatherService.initialize(config);

    logger.info("Creating weather service with cfg: "+ config);

    return weatherService;
  }

  @Override
  public Reactor createReactor(Invoker invoker) {
    Reactor reactor = null; 
    reactor = Config.loadAndInstantiate(environmentReader, Config.SKYCAVE_REACTOR_IMPLEMENTATION, reactor);

    // Read in the server configuration
    ServerConfiguration config = 
        new ServerConfiguration(environmentReader, Config.SKYCAVE_APPSERVER);
    reactor.initialize(invoker, config);

    return reactor;
  }

}
