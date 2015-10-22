package cloud.cave.extension;

import cloud.cave.ipc.Marshaling;
import cloud.cave.server.common.Command;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import org.json.simple.JSONObject;

/**
 * A command pattern based implementation of a jump command, that allows a
 * player to instantly move to a specific room.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class CountRoomCommand extends AbstractCommand implements Command {

  @Override
  public JSONObject execute(String... parameters) {

    
    int numberOfRooms = storage.computeCountOfRooms();

    JSONObject reply =
        Marshaling.createValidReplyWithReturnValue(""+numberOfRooms, "Number of Rooms");

    return reply;
  }

}
