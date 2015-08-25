package cloud.cave.config;

import cloud.cave.common.*;

/**
 * Config encapsulates the names of environment variables that must be set in
 * order for the factories to create proper delegate configurations of the
 * server and client side of the cave system.
 * <p>
 * It also provides the 'failFastRead()' method that provides a way to read
 * environment variables that fails immediately in case a environment variable
 * is not correctly set.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class Config {

  /**
   * Environment variable that must be set to 'name:port' of the endpoint for
   * the application server. In case of a cluster, separate each endpoint with
   * ';'.
   */
  public static final String SKYCAVE_APPSERVER = "SKYCAVE_APPSERVER";

  /**
   * Environment variable that must be set to the 'name:port' of the database
   * server. Separate with ';' in case of a cluster.
   */
  public static final String SKYCAVE_DBSERVER = "SKYCAVE_DBSERVER";

  /**
   * Environment variable that must be set to the 'name:port' of the
   * subscription server end point.
   */
  public static final String SKYCAVE_SUBSCRIPTIONSERVER = "SKYCAVE_SUBSCRIPTIONSERVER";

  /**
   * Environment variable that must be set to the 'name:port' of the weather
   * server end point.
   */
  public static final String SKYCAVE_WEATHERSERVER = "SKYCAVE_WEATHERSERVER";

  /**
   * Environment variable that must be set to the fully qualified class name of
   * the class implementing the cave storage interface. This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_CAVESTORAGE_IMPLEMENTATION = "SKYCAVE_CAVESTORAGE_IMPLEMENTATION";

  /**
   * Environment variable that must be set to the fully qualified class name of
   * the class implementing the subscription service interface. This class must
   * be in the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_SUBSCRIPTION_IMPLEMENTATION = "SKYCAVE_SUBSCRIPTION_IMPLEMENTATION";

  /**
   * Environment variable that must be set to the fully qualified class name of
   * the class implementing the weather service interface. This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_WEATHER_IMPLEMENATION = "SKYCAVE_WEATHER_IMPLEMENTATION";

  /**
   * Environment variable that must be set to the fully qualified class name of
   * the class implementing the weather service interface. This class must be in
   * the classpath and will be loaded at runtime by the ServerFactory.
   */
  public static final String SKYCAVE_REACTOR_IMPLEMENTATION = "SKYCAVE_REACTOR_IMPLEMENTATION";

  /**
   * Environment variable that must be set to the fully qualified class name of
   * the class implementing the client request handler interface. This class must be in
   * the classpath and will be loaded at runtime by the ClientFactory.
   */
  public static final String SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION = "SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION";
  
  /**
   * Read an environment variable using the given reader strategy. Fail
   * immediately in case the environment variable is not set.
   * 
   * @param environmentReader
   *          the environment reader strategy to be used to read the variable
   * @param environmentVariable
   *          the variable to be read
   * @return the value of the variable in the environment
   * @throws CaveConfigurationNotSetException
   *           in case the variable is not set
   */
  public static String failFastRead(
      EnvironmentReaderStrategy environmentReader, String environmentVariable) {
    String value = environmentReader.getEnv(environmentVariable);
    if (value == null || value.equals("")) {
      throw new CaveConfigurationNotSetException(
          "ConfigurationError: The configuration is not defined because"
              + " the environment variable '" + environmentVariable
              + "' is not set.");
    }
    return value;
  }

  /**
   * Generic method to load and instantiate object of type T which is on the
   * path given by environment variable envVariable.
   * 
   * @param <T>
   *          type parameter defining the class type of the object to
   *          instantiate
   * @param environmentReader
   *          the strategy for reading the environment variable
   * @param envVariable
   *          the environment variable that holds the full path to the class to
   *          load
   * @param theObject
   *          actually a dummy but its type tells the method the generic type
   * @return object of type T loaded from the fully qualified type name given by
   *         the environment variable envVariabl
   */
  @SuppressWarnings("unchecked")
  public static <T> T loadAndInstantiate(EnvironmentReaderStrategy environmentReader,
      String envVariable,
      T theObject) {
    // read full path of class to load
    String qualifiedNameOfType;
    qualifiedNameOfType = 
        Config.failFastRead(environmentReader, envVariable);

    // Use java reflection to read in the class
    Class<?> theClass = null;
    try {
      theClass = Class.forName(qualifiedNameOfType);
    } catch (ClassNotFoundException e) {
      throw new CaveClassNotFoundException("EnvironmentFactory: Class '"
          +qualifiedNameOfType+"' is not found."+
          "Environment Variable : "+envVariable);
    }
    
    // Next, instantiate object from the class 
    try {
      theObject = (T) theClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new CaveClassInstantiationException("EnvironmentFactory: Class '"
          +qualifiedNameOfType+"' could not be instantiated!", e);
    }
    
    return theObject;
  }
}
