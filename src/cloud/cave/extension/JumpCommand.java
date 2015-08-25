package cloud.cave.extension;

import org.json.simple.JSONObject;

import cloud.cave.ipc.Marshaling;
import cloud.cave.server.common.*;

/**
 * A command pattern based implementation of a jump command, that allows a
 * player to instantly move to a specific room.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class JumpCommand extends AbstractCommand implements Command {

  @Override
  public JSONObject execute(String... parameters) {
    String positionString = parameters[0];
    
    // Validate that the position is known in the cave
    RoomRecord room = storage.getRoom(positionString);
    if ( room == null ) {
      return Marshaling.createValidReplyWithReturnValue("false", "JumpCommand failed, room " 
          + positionString + " does not exist in the cave.");
    }
    
    PlayerRecord pRecord = storage.getPlayerByID(playerID);
    pRecord.setPositionAsString(positionString);
    storage.updatePlayerRecord(pRecord);

    JSONObject reply =
        Marshaling.createValidReplyWithReturnValue("true", positionString);

    return reply;
  }

}
