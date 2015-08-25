package cloud.cave.server;

import org.json.simple.*;

import cloud.cave.domain.*;
import cloud.cave.ipc.*;

public class CaveDispatcher implements Dispatcher {
  private Cave cave;

  public CaveDispatcher(Cave cave) {
    this.cave = cave;
  }

  @Override
  public JSONObject dispatch(String methodKey, String playerID,
      String sessionID, String parameter1, JSONArray parameterList) {
    JSONObject reply = null;

    // === LOGIN
    if (methodKey.equals(MarshalingKeys.LOGIN_METHOD_KEY)) {
      String loginName = parameter1;
      String password = parameterList.get(0).toString();
      Login result = cave.login(loginName, password);
      if (LoginResult.isValidLogin(result.getResultCode())) {
        String playerId = result.getPlayer().getID();
        String playerName = result.getPlayer().getName();
        String sessionId = result.getPlayer().getSessionID();
        // add the player to the cache
        // cache.add(sessionId, result.getPlayer());
        reply = Marshaling.createValidReplyWithReturnValue(result
            .getResultCode().toString(), playerId, playerName, sessionId);
      } else {
        // The player id is set to "" as we do not know
        reply = Marshaling.createValidReplyWithReturnValue(result
            .getResultCode().toString());
      }
    }
    // === LOGOUT
    else if (methodKey.equals(MarshalingKeys.LOGOUT_METHOD_KEY)) {
      LogoutResult result = cave.logout(playerID);

      // remove the player's session from the cache
      // cache.remove(sessionID);
      
      reply = Marshaling.createValidReplyWithReturnValue(result.toString());
    }
    // === DESCRIBE CONFIGURATION
    else if (methodKey.equals(MarshalingKeys.DESCRIBE_CONFIGURATION_METHOD_KEY)) {
      
      reply = Marshaling.createValidReplyWithReturnValue(cave.describeConfiguration());
    }
    // No need for a 'default case' as the returned null value will
    // be caught in the caller.

    return reply;
  }

}
