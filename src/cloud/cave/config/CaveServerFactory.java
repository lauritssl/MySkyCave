package cloud.cave.config;

import cloud.cave.ipc.*;
import cloud.cave.service.*;

/**
 * Abstract factory (FRS, page 217) interface for creating delegates for the
 * server side cave. For production, use the implementation based
 * upon reading environment variables.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface CaveServerFactory {

  /**
   * Create and return the storage strategy, the binding to the storage system
   * that holds all data related to the cave: players, rooms, etc.
   * <p>
   * In case of an external storage system (a database connection) the
   * factory will return a fully initialized and open connection.
   * 
   * @return a binding to the storage system
   */
  CaveStorage createCaveStorage();

  /**
   * Create and return an initialized connector to the subscription service.
   * 
   * @return a connector to the subscription service
   */
  SubscriptionService createSubscriptionServiceConnector();

  /**
   * Create and return an initialized connector to the weather service.
   * 
   * @return a connector to the weather service
   */
  WeatherService createWeatherServiceConnector();

  /**
   * Create and return the reactor object that binds the server invoker
   * to the particular OS and the IPC system chosen.
   * 
   * @param invoker
   *          the invoker to dispatch all events from clients to the
   *          server objects
   * 
   * @return the reactor
   */
  Reactor createReactor(Invoker invoker);

}
