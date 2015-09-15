package cloud.cave.doubles;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.ipc.Invoker;
import cloud.cave.ipc.Reactor;
import cloud.cave.service.CaveStorage;
import cloud.cave.service.CircuitBreakerSubscriptionService;
import cloud.cave.service.SubscriptionService;
import cloud.cave.service.WeatherService;

/**
 * Concrete factory for making making delegates that are all test doubles.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class AllTestCircuitBreakerFactory implements CaveServerFactory {
  private SubscriptionService ss;
  @Override
  public CaveStorage createCaveStorage() {
    CaveStorage storage = new FakeCaveStorage();
    storage.initialize(null); // the fake storage needs no server configuration object
    return storage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector() {
    SubscriptionService service = ss;
    return service;
  }

  @Override
  public WeatherService createWeatherServiceConnector() {
    WeatherService service = new TestStubWeatherService();
    service.initialize(null); // no config object required
    return service;
  }

  @Override
  public Reactor createReactor(Invoker invoker) {
    // The reactor is not presently used in the test cases...
    return null;
  }

  public void setSubscriptionService(SubscriptionService ss){
    this.ss = ss;
  }


}
