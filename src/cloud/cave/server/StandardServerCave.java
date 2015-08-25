package cloud.cave.server;

import java.util.UUID;

import org.slf4j.*;

import cloud.cave.common.*;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.domain.*;
import cloud.cave.ipc.CaveIPCException;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * The standard server side implementation of the Cave. Just as the server side
 * player, this implementation communicates directly with the storage layer to
 * achieve it behavior.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class StandardServerCave implements Cave {

  private CaveStorage storage;
  private SubscriptionService subscriptionService;
  private WeatherService weatherService;
  private PlayerSessionCache sessionCache;

  private Logger logger;

  public StandardServerCave(CaveServerFactory factory) {
    storage = factory.createCaveStorage();
    
    subscriptionService = factory
        .createSubscriptionServiceConnector();

    weatherService = factory.createWeatherServiceConnector();
    
    // TODO Currently the session cache strategy is not injected
    sessionCache = new SimpleInMemoryCache();
    
    logger = LoggerFactory.getLogger(StandardServerCave.class);
  }

  /**
   * Given a loginName and password (like '201017201','123') contact the
   * subscription storage to validate that the player is registered. If valid,
   * create the player avatar. Return the result of the login
   * 
   * @param loginName
   *          the loginName which the player uses to identify his/her account in
   *          the cave
   * @param password
   *          the password associated with the account
   * @return the result of the login
   */
  @Override
  public Login login(String loginName, String password) {
    Login result = null;

    // Fetch the subscription for the given loginName
    SubscriptionRecord subscription = null;
    String errorMsg = null;
    try {
      subscription = subscriptionService.lookup(loginName, password);
    } catch (CaveIPCException e) {
      errorMsg="Lookup failed on subscription service due to IPC exception:"+e.getMessage();
      logger.error(errorMsg);
    }
    
    if (subscription==null) {
      return new LoginRecord(LoginResult.LOGIN_FAILED_SERVER_ERROR);
    }
    // Check all the error conditions and 'fail fast' on them...
    if (subscription.getErrorCode() == SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN) {
      return new LoginRecord(LoginResult.LOGIN_FAILED_UNKNOWN_SUBSCRIPTION);
    }
    
    // Now the subscription is assumed to be a valid player
    String playerID = subscription.getPlayerID();
    
    // Create id of session as a random UUID
    String sessionID = UUID.randomUUID().toString();
    
    // Enter the player, creating the player's session in the cave
    // (which may overwrite an already ongoing session which is then
    // implicitly invalidated).
    LoginResult theResult = startPlayerSession(subscription, sessionID);

    boolean validLogin = LoginResult.isValidLogin(theResult);
    if ( ! validLogin ) {
      return new LoginRecord(theResult);
    }

    // Create player domain object
    Player player = new StandardServerPlayer(playerID, storage, weatherService, sessionCache);
    
    // Cache the player session for faster lookups
    sessionCache.add(playerID, player);
    
    // And finalize the login result
    result = new LoginRecord(player, theResult);

    return result;
  }

  /** Initialize a player session by updating/preparing the storage system
   * and potentially clear the cache of previous sessions.
   * @param subscription the record of the subscription to start a session on
   * @param sessionID ID of the session assigned to this login
   * @return result of the login which is always a valid login, but
   * may signal a 'second login' that overrules a previous one.
   */
  private LoginResult startPlayerSession(SubscriptionRecord subscription,
      String sessionID) {
    LoginResult result = LoginResult.LOGIN_SUCCESS; // Assume success
    
    // get the record of the player from storage
    PlayerRecord playerRecord = storage.getPlayerByID(subscription.getPlayerID());
    
    if (playerRecord == null) {
      // Apparently a newly registered player, so create the record
      // and add it to the cave storage
      String position = new Point3(0, 0, 0).getPositionString();
      playerRecord = new PlayerRecord(subscription, position, sessionID);
      storage.updatePlayerRecord(playerRecord);
    } else {
      // Player has been seen before; if he/she has an existing
      // session ("= is in cave") we flag this as a warning,
      // and clear the cache entry
      if (playerRecord.isInCave()) {
        result = LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN;
      }
      // update the session id in the storage system
      playerRecord.setSessionId(sessionID);
      storage.updatePlayerRecord(playerRecord);
    }

    return result;
  }

  @Override
  public LogoutResult logout(String playerID) {
    // ensure that the player is known by and in the cave
    PlayerRecord player = storage.getPlayerByID(playerID);
    
    if (!player.isInCave()) {
      return LogoutResult.PLAYER_NOT_IN_CAVE;
    }

    // reset the session  to indicate the player is no longer around
    player.setSessionId(null);

    // and update the record in the storage
    storage.updatePlayerRecord(player);
    
    // and clean up the cache
    sessionCache.remove(playerID);
    
    return LogoutResult.SUCCESS;
  }

  @Override
  public String describeConfiguration() {
    String cfg = "StandardServerCave configuration:\n";
    cfg += "  CaveStorage: "+ storage.getClass().getName()+" / cfg: "+storage.getConfiguration() + "\n";
    cfg += "  SubscriptionService: "+ subscriptionService.getClass().getName()+" / cfg: "+subscriptionService.getConfiguration() + "\n";
    cfg += "  WeatherService: "+ weatherService.getClass().getName()+" / cfg: "+weatherService.getConfiguration() + "\n";
    return cfg;
  }

  public PlayerSessionCache getCache() {
   return sessionCache;
  }
}
