package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.junit.*;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.server.common.*;
import cloud.cave.service.CaveStorage;

/** Testing the access pattern of the DB regarding
 * updates and simple queries of the player record.
 * <p>
 * Demonstrates the use of a spy to inspect behaviour
 * of the the cave and player implementations.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestStorageAccess {
  private Cave cave;
  private SpyCaveStorage spy;
  
  @Before
  public void setup() {
    CaveServerFactory factory = new AllTestDoubleFactory() {
      @Override
      public CaveStorage createCaveStorage() {
        CaveStorage storage = new FakeCaveStorage();
        storage.initialize(null);
        // Decorate the storage with a spy that
        // monitors DB access patterns
        spy = new SpyCaveStorage(storage);
        return spy;
      }

    };
    cave = new StandardServerCave(factory);
  }

  @Test
  public void shouldSpyAccessPatternInDB() {
    Login loginResult = cave.login( "magnus_aarskort", "312");
    Player p1 = loginResult.getPlayer();
    assertNotNull(p1);

    // assert the number of database updates and queries
    assertThat(spy.getPlayerUpdateCount(), is(1));
    assertThat(spy.getPlayerGetCount(), is(2));

    // assert the number of updates and queries
    
    // Uncomment the statement below to get full stack traces of
    // where the storage queries are made in the player impl.
    // spy.setTracingTo(true);
    p1.getLongRoomDescription();
    spy.setTracingTo(false);
    
    assertThat(spy.getPlayerUpdateCount(), is(1)); // no updates
    assertThat(spy.getPlayerGetCount(), is(3)); // and a single query extra
    
    LogoutResult result = cave.logout(p1.getID());
    assertNotNull("The result of the logout is null", result);
    assertEquals(LogoutResult.SUCCESS, result);
  }

}


class SpyCaveStorage implements CaveStorage {

  private CaveStorage decoratee;
  private boolean traceOn;
  
  public SpyCaveStorage(CaveStorage decoratee) {
    super();
    this.decoratee = decoratee;
    traceOn = false;
  }

  public void setTracingTo(boolean b) {
    traceOn = b; 
  }

  public RoomRecord getRoom(String positionString) {
    return decoratee.getRoom(positionString);
  }


  public boolean addRoom(String positionString, RoomRecord description) {
    return decoratee.addRoom(positionString, description);
  }


  public void initialize(ServerConfiguration config) {
    decoratee.initialize(config);
  }


  public List<Direction> getSetOfExitsFromRoom(String positionString) {
    return decoratee.getSetOfExitsFromRoom(positionString);
  }


  private int getCount = 0;
  @SuppressWarnings("static-access")
  public PlayerRecord getPlayerByID(String playerID) {
    getCount++;
    if (traceOn) Thread.currentThread().dumpStack();
    return decoratee.getPlayerByID(playerID);
  }
  
  public int getPlayerGetCount() {
    return getCount;
  }

  public void disconnect() {
    decoratee.disconnect();
  }


  private int updateCount = 0;
  public void updatePlayerRecord(PlayerRecord record) {
    updateCount++;
    decoratee.updatePlayerRecord(record);
  }

  public int getPlayerUpdateCount() {
    return updateCount;
  }



  public ServerConfiguration getConfiguration() {
    return decoratee.getConfiguration();
  }


  public List<PlayerRecord> computeListOfPlayersAt(String positionString) {
    return decoratee.computeListOfPlayersAt(positionString);
  }


  public int computeCountOfActivePlayers() {
    return decoratee.computeCountOfActivePlayers();
  }

}
