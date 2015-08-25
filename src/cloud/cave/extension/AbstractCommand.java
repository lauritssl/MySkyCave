package cloud.cave.extension;

import cloud.cave.server.common.Command;
import cloud.cave.service.*;

/**
 * Abstract base class for command instances. Simply stores the playerID and
 * storage service reference.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public abstract class AbstractCommand implements Command {

  protected CaveStorage storage;
  protected String playerID;

  public AbstractCommand() {
    super();
  }

  @Override
  public void setStorageService(CaveStorage storage) {
    this.storage = storage;
  }

  @Override
  public void setPlayerID(String playerID) {
    this.playerID = playerID;
  }

}