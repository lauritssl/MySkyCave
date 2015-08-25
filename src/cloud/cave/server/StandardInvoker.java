package cloud.cave.server;

import java.util.*;

import org.json.simple.*;
import org.slf4j.*;

import cloud.cave.domain.*;
import cloud.cave.ipc.*;

/** Standard implementation of the Invoker.
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 *
 */
public class StandardInvoker implements Invoker {
  private Logger logger;
  private Dispatcher dispatcher;
  
  private Map<String,Dispatcher> mapKey2Dispatch;

  /** Create an invoker that dispatches requests
   * using default dispatching, that is the
   * set of methods known in the initial release of
   * SkyCave.
   * @param cave the cave that all requests manipulate
   */
  public StandardInvoker(Cave cave) {
    // Create the map that maps from class prefix to
    // dispatcher for that particular class type,
    // see Reactor pattern and 'identifyDispather' method.
    mapKey2Dispatch = new HashMap<String, Dispatcher>();
    mapKey2Dispatch.put(MarshalingKeys.CAVE_TYPE_PREFIX, new CaveDispatcher(cave));
    mapKey2Dispatch.put(MarshalingKeys.PLAYER_TYPE_PREFIX, new PlayerDispatcher(cave));
    
    logger = LoggerFactory.getLogger(StandardInvoker.class);
  }
  
  /** Create an invoker that dispatches requests
   * to the given cave using a given dispatcher.
   * @param cave the cave that all requests manipulate
   * @param mapTypePrefixToDispatchers a Map that
   * maps strings to dispatchers; the key string must
   * be one of the type prefixes, like "player-", defined
   * in the MarshalingKeys.
   */
  public StandardInvoker(Cave cave, Map<String,Dispatcher> mapTypePrefixToDispatchers) {
    mapKey2Dispatch = mapTypePrefixToDispatchers;
    logger = LoggerFactory.getLogger(StandardInvoker.class);
  }
  
  
  @Override
  public JSONObject handleRequest(JSONObject request) {
    JSONObject reply = null;  
 
    // Extract the common parameters from the request object and assign
    // them names that reflect their meaning
    String playerID = request.get(MarshalingKeys.PLAYER_ID_KEY).toString();
    String sessionID = request.get(MarshalingKeys.PLAYER_SESSION_ID_KEY).toString(); 
    String methodKey = request.get(MarshalingKeys.METHOD_KEY).toString();
    String parameter1 = "";
    Object parameter1AsObj = request.get(MarshalingKeys.PARAMETER_HEAD_KEY);
    if ( parameter1AsObj != null ) {
      parameter1 = parameter1AsObj.toString();
    }
    JSONArray parameterList = 
        (JSONArray) request.get(MarshalingKeys.PARAMETER_TAIL_KEY);
 
    // Dispatch the event (POSA vol 4 Reactor code)
    dispatcher = identifyDispatcher(methodKey);
    
    // We may get a null object back if the method key is ill formed
    // thus guard the dispatch call
    if (dispatcher != null) {
      // Next, do the dispatching - based upon the parameters, call
      // the proper method on the proper object
      reply = dispatcher.dispatch(methodKey, playerID, sessionID, parameter1,
          parameterList);
    }    
    // UNHANDLED METHOD 
    if (reply == null) {
      reply = Marshaling.createInvalidReplyWithExplantion(StatusCode.SERVER_UNKNOWN_METHOD_FAILURE,
          "StandardInvoker.handleRequest: Unhandled request as the method key "+methodKey +
          " is unknown. Full request="+request.toString());
      logger.warn("handleRequest: Unhandled request as the method key "+methodKey +
          " is unknown. Full request="+request.toString());
    }

    return reply;
  }

  /** Identify the dispatcher appropriate for the given method.
   * Corresponds to the identify_handler(event) in Reactor
   * pattern.
   * <p>
   * Presently relies on mangled method names, i.e. that
   * a method on the player starts with 'play' and a method
   * on cave starts with 'cave'. 
   * 
   * @param methodKey key of the method, see MarshalingKeys
   * @return the appropriate dispatcher for the class containing
   * that particular method or null if the method key is ill-formed
   */
  private Dispatcher identifyDispatcher(String methodKey) {
    Dispatcher dsp = dispatcher;
    int firstDash = methodKey.indexOf("-");
    String key = methodKey.substring(0, firstDash+1);
    dsp = mapKey2Dispatch.get(key);
    return dsp;
  }
}
