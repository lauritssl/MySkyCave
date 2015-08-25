package cloud.cave.doubles;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.ipc.*;
import cloud.cave.service.*;

/**
 * Concrete factory for making making delegates that are all test doubles.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class AllTestDoubleFactory implements CaveServerFactory {

  @Override
  public CaveStorage createCaveStorage() {
    CaveStorage storage = new FakeCaveStorage();
    storage.initialize(null); // the fake storage needs no server configuration object
    return storage;
  }

  @Override
  public SubscriptionService createSubscriptionServiceConnector() {
    SubscriptionService service = new TestStubSubscriptionService();
    service.initialize(null); // no config object required for the stub
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


}
