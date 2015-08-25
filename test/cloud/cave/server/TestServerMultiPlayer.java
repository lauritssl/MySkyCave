package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.junit.*;

import cloud.cave.common.*;
import cloud.cave.domain.*;

/** Testing that the cave can handle having multiple
 * players in the cave at the same time.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestServerMultiPlayer {

  private Cave cave;
  
  private Player p1, p2;

  @Before
  public void setup() {
    cave = CommonCaveTests.createTestDoubledConfiguredCave();
  }
 
  private void enterBothPlayers() {
    Login loginResult = cave.login( "magnus_aarskort", "312");
    p1 = loginResult.getPlayer();
    
    loginResult = cave.login( "mathilde_aarskort", "321");
    p2 = loginResult.getPlayer();
  }

  @Test
  public void shouldAllowIndependentMoves() {
    enterBothPlayers();
    
    // p1 moves west, only one left in entry room
    p1.move(Direction.WEST);

    // p2 moves east, none left in entry room
    p2.move(Direction.EAST);

    // The room descriptions are different
    assertNotEquals( p1.getShortRoomDescription(), p2.getShortRoomDescription() );
    
    p2.move(Direction.WEST);
    assertNotEquals( p1.getShortRoomDescription(), p2.getShortRoomDescription() );
    
    p2.move(Direction.WEST);
    assertEquals( p1.getShortRoomDescription(), p2.getShortRoomDescription() );
  }
  
  @Test
  public void shouldSeeOtherPlayersInSameLocation() {
    enterBothPlayers();
    p1.move(Direction.WEST);
    
    // only myself
    assertEquals( 1, p1.getPlayersHere().size());
    List<String> playersHere;
    playersHere = p1.getPlayersHere();
    assertThat( playersHere, hasItem(p1.getName()));
    
    // move p2 there
    p2.move(Direction.WEST);
    assertEquals( 2, p2.getPlayersHere().size());
    playersHere = p2.getPlayersHere();
    assertThat( playersHere, hasItems(p1.getName(), p2.getName()));
  }
  
  @Test
  public void shouldProvideBothPlayersInLongDescription() {
    enterBothPlayers();
    String longDescription = p1.getLongRoomDescription();
    // System.out.println(longDescription);

    assertThat( longDescription, containsString("You see other players:"));
    assertThat( longDescription, containsString("Magnus"));
    assertThat( longDescription, containsString("Mathilde"));
    
    // move mathilde out of the room
    p2.move(Direction.WEST);
    longDescription = p1.getLongRoomDescription();

    assertThat( longDescription, containsString("Magnus"));
    assertThat( longDescription, not(containsString("Mathilde")));
  }
  
  @Test
  public void shouldSeeLongDescriptionUpdateWhenOtherPlayerLogOut() {
    enterBothPlayers();
    CommonCaveTests.shouldAllowLoggingOutMagnus(cave, p1);
    String longDescription = p2.getLongRoomDescription();
    // System.out.println(longDescription);
    
    assertThat( longDescription, containsString("You see other players:"));
    assertThat( longDescription, not(containsString("Magnus")));
    assertThat( longDescription, containsString("Mathilde"));
  }
  
  @Test
  public void shouldSeePlayersInRoom() {
    Login loginResult = cave.login( "magnus_aarskort", "312");
    p1 = loginResult.getPlayer();

    CommonPlayerTests.shouldSeeMathildeComingInAndOutOfRoomDuringSession(cave, p1);
  }

}


