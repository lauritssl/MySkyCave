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

  @Test
  public void shouldBeAbleToAddMessage() {
    String message = "Test";
    spy.addMessage(new Point3(0, 0, 0).getPositionString(), message);
    assertEquals(spy.getMessageList(new Point3(0, 0, 0).getPositionString()).get(0), message);
  }

  @Test
  public void shouldBeAbleToAddTwoMessagesToSameRoom() {
    String msg1 = "Test1";
    String msg2 = "Test2";
    spy.addMessage(new Point3(0, 0, 0).getPositionString(), msg1);
    spy.addMessage(new Point3(0, 0, 0).getPositionString(), msg2);
    assertEquals(spy.getMessageList(new Point3(0, 0, 0).getPositionString()).get(0), msg1);
    assertEquals(spy.getMessageList(new Point3(0, 0, 0).getPositionString()).get(1), msg2);
  }

  @Test
  public void shouldReturnEmptyListIfNoMessages(){
    assertThat(spy.getMessageList(new Point3(0, 0, 0).getPositionString()).size(), is(0));
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

  @Override
  public void addMessage(String positionString, String message) {
    decoratee.addMessage(positionString, message);
  }

  @Override
  public List<String> getMessageList(String positionString) {
    return decoratee.getMessageList(positionString);
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


  public List<PlayerRecord> computeListOfPlayersAt(String positionString, int from, int to) {
    return decoratee.computeListOfPlayersAt(positionString, from, to);
  }


  public int computeCountOfActivePlayers() {
    return decoratee.computeCountOfActivePlayers();
  }

  @Override
  public String sessionGet(String playerID) {
    return decoratee.sessionGet(playerID);
  }

  @Override
  public void sessionAdd(String playerID, Player player) {
    decoratee.sessionAdd(playerID, player);
  }

  @Override
  public void sessionRemove(String playerID) {
    decoratee.sessionRemove(playerID);
  }

  @Override
  public int computeCountOfRooms() {
    return  decoratee.computeCountOfRooms();
  }

}
