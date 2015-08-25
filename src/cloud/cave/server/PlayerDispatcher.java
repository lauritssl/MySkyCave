package cloud.cave.server;

import java.util.List;

import org.json.simple.*;

import cloud.cave.common.PlayerSessionExpiredException;
import cloud.cave.domain.*;
import cloud.cave.ipc.*;

/** Dispatcher implementation covering all the methods
 * belonging to calls to Player.
 *
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class PlayerDispatcher implements Dispatcher {
  
  private PlayerSessionCache cache;

  public PlayerDispatcher(Cave cave) {
    // TODO: Nasty hack - but we need to get that cache; and the
    // dispatcher is of course only used on the server side...
    StandardServerCave scave = (StandardServerCave) cave;
    cache = scave.getCache(); 
  }

  @Override
  public JSONObject dispatch(String methodKey, String playerID,
      String sessionID, String parameter1, JSONArray parameterList) {
    JSONObject reply = null;
    try {
      // Fetch the server side player object from cache
      Player player = cache.get(playerID); 

      // Access control of the 'Blizzard' variant: the last
      // login (= session) is the one winning. If the session id
      // coming from the client differs from the one cached here
      // in the server means two different clients are accessing
      // the same player object. However we assign a new session
      // id upon each login thus if they differ, the client
      // calling us has the 'old session' and must thus be
      // told that he/she cannot control the avatar any more.
      if (!sessionID.equals(player.getSessionID())) {
        throw new PlayerSessionExpiredException(
            "PlayerDispatcher: The session for player " + player.getID()
                + " is no longer valid (Client session="+sessionID+"/Server cached session="
                +player.getSessionID()+").");
      }

      // === SHORT ROOM
      if (methodKey
          .equals(MarshalingKeys.GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY)) {
        reply = Marshaling.createValidReplyWithReturnValue(player
            .getShortRoomDescription());
      }
      // === LONG ROOM
      else if (methodKey
          .equals(MarshalingKeys.GET_LONG_ROOM_DESCRIPTION_METHOD_KEY)) {
        reply = Marshaling.createValidReplyWithReturnValue(player
            .getLongRoomDescription());
      }
      // === REGION
      else if (methodKey.equals(MarshalingKeys.GET_REGION_METHOD_KEY)) {
        reply = Marshaling.createValidReplyWithReturnValue(player.getRegion()
            .toString());
      }
      // === POSITION
      else if (methodKey.equals(MarshalingKeys.GET_POSITION_METHOD_KEY)) {
        reply = Marshaling
            .createValidReplyWithReturnValue(player.getPosition());
      }
      // === PLAYERS HERE
      else if (methodKey.equals(MarshalingKeys.GET_PLAYERS_HERE_METHOD_KEY)) {
        List<String> playersHere = player.getPlayersHere();
        String[] asArray = new String[playersHere.size()];
        playersHere.toArray(asArray);

        // It is easier to not use the HEAD and just put the array in the TAIL
        // of the answer
        reply = Marshaling.createValidReplyWithReturnValue("notused", asArray);
      }
      // === EXIT SET
      else if (methodKey.equals(MarshalingKeys.GET_EXITSET_METHOD_KEY)) {
        List<Direction> exitSet = player.getExitSet();
        String[] asArray = new String[exitSet.size()];
        int i = 0;
        // Convert each enum to string representation
        for (Direction d : exitSet) {
          asArray[i++] = d.toString();
        }
        // It is easier to not use the HEAD and just put the array in the TAIL
        // of the answer
        reply = Marshaling.createValidReplyWithReturnValue("notused", asArray);
      }
      // === MOVE
      else if (methodKey.equals(MarshalingKeys.MOVE_METHOD_KEY)) {
        // move(direction)
        Direction direction = Direction.valueOf(parameter1);
        boolean isValid = player.move(direction);

        reply = Marshaling.createValidReplyWithReturnValue("" + isValid);
      }
      // === DIG
      else if (methodKey.equals(MarshalingKeys.DIG_ROOM_METHOD_KEY)) {
        Direction direction = Direction.valueOf(parameter1);
        String description = parameterList.get(0).toString();
        boolean isValid = player.digRoom(direction, description);

        reply = Marshaling.createValidReplyWithReturnValue("" + isValid);
      }
      // === EXECUTE
      else if (methodKey.equals(MarshalingKeys.EXECUTE_METHOD_KEY)) {
        String commandName = parameter1;
        String[] parameters = new String[3];
        int i = 0;
        for (Object obj : parameterList) {
          parameters[i] = obj.toString();
          i++;
        }

        reply = player.execute(commandName, parameters);
      }
    } catch (PlayerSessionExpiredException exc) {
      reply = Marshaling.createInvalidReplyWithExplantion(StatusCode.SERVER_PLAYER_SESSION_EXPIRED_FAILURE,
          exc.getMessage());
    }
    return reply;
  }

}
