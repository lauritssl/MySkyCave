package cloud.cave.extension;

import org.json.simple.JSONObject;

import cloud.cave.ipc.Marshaling;
import cloud.cave.server.common.*;

/**
 * An implementation of a command that 'flies the player home' to the entry room
 * (0,0,0).
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class HomeCommand extends AbstractCommand implements Command {

  @Override
  public JSONObject execute(String... parameters) {
    Point3 home = new Point3(0, 0, 0);
    PlayerRecord pRecord = storage.getPlayerByID(playerID);
    pRecord.setPositionAsString(home.getPositionString());
    storage.updatePlayerRecord(pRecord);

    JSONObject reply;
    reply =
        Marshaling.createValidReplyWithReturnValue(home.getPositionString());
    return reply;
  }


}
