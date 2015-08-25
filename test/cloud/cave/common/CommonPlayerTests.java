package cloud.cave.common;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.*;

import cloud.cave.domain.*;
import cloud.cave.server.common.Point3;

/** A collection of tests regarding player that is
 * repeated in both the server side testing (TDD
 * of the basic behaviour of an implementation of
 * player) as well as the client side proxy testing (TDD 
 * of the client proxy's marshalling and demarshalling
 * of request/reply objects from the server request
 * handler).
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 *
 */

public class CommonPlayerTests {
  
  public static void shouldAccessSimpleAttributes(Player player) {
    assertEquals( "Mikkel", player.getName() );
    assertEquals( "user-001", player.getID() );
    assertEquals( Region.AARHUS, player.getRegion() );
    assertEquals( "(0,0,0)", player.getPosition() );
  }

  public static void shouldTestCoordinateTranslations(Player player) {
    assertEquals( "(0,0,0)", player.getPosition());

    player.move(Direction.NORTH);
    assertEquals( "(0,1,0)", player.getPosition());    
    player.move(Direction.SOUTH);
    assertEquals( "(0,0,0)", player.getPosition());    

    player.move(Direction.UP);
    assertEquals( "(0,0,1)", player.getPosition());    
    player.move(Direction.DOWN);
    assertEquals( "(0,0,0)", player.getPosition());    

    player.move(Direction.WEST);
    assertEquals( "(-1,0,0)", player.getPosition());    

    player.move(Direction.EAST);
    assertEquals( "(0,0,0)", player.getPosition());    
  }

  public static void shouldAllowPlayerToDigNewRooms(Player player) {
    boolean valid = player.digRoom(Direction.DOWN, "Road Cellar");
    assertTrue("It is allowed to dig room in down direction", valid);
    
    valid = player.move(Direction.DOWN);
    assertTrue( player.getShortRoomDescription().contains("Road Cellar"));
  }

  public static void shouldNotAllowDigAtEast(Player player) {
    boolean allowed = player.digRoom(Direction.EAST, "Santa's cave.");
    assertFalse("It should not be possible to dig east, as the well house is there.",
        allowed );
  }

  public static void shouldProvideLongDescription(Player player) {
    String longDescription = player.getLongRoomDescription();
    
    assertTrue( longDescription.contains("There are exits in"));
    assertTrue( longDescription.contains("NORTH"));
    assertTrue( longDescription.contains("WEST"));
    assertTrue( longDescription.contains("EAST"));
    assertTrue( ! longDescription.contains("SOUTH"));
    
    assertThat( longDescription, containsString("You see other players:"));
    assertThat( longDescription, containsString("[0] Mikkel"));
  }

  public static void shouldShowExitsForPlayersPosition(Player player) {
    String description = player.getLongRoomDescription();
    assertTrue( description.contains("NORTH"));
    assertTrue( description.contains("WEST"));
    assertTrue( description.contains("EAST"));
    assertFalse( description.contains("SOUTH"));
    
    // move east, which only as one exit, back west
    player.move(Direction.EAST);
    description = player.getLongRoomDescription();
    assertFalse( description.contains("NORTH"));
    assertTrue( description.contains("WEST"));
    assertFalse( description.contains("EAST"));
    assertFalse(  description.contains("SOUTH"));
  }

  public static void shouldGetProperExitSet(Player player) {
    List<Direction> exitSet = player.getExitSet();
    assertThat( exitSet.size(), is(4));
    
    assertThat( exitSet, hasItem(Direction.NORTH));
    assertThat( exitSet, hasItem(Direction.WEST));
    assertThat( exitSet, hasItem(Direction.UP));
    assertThat( exitSet, hasItem(Direction.EAST));
  }

  public static void shouldBeAtPositionOfLastLogout(Cave cave, Player player) {
    Login loginResult;
    Point3 pos = new Point3(0, 0, 0);
    assertThat(player.getPosition(), is(pos.getPositionString()));
    
    // Move mathilde
    player.move(Direction.EAST);
    String newPos = player.getPosition();
    
    // Log her out
    LogoutResult logoutResult = cave.logout(player.getID());
    
    assertThat(logoutResult, is(LogoutResult.SUCCESS));

    // Log her back in
    loginResult = cave.login( "mathilde_aarskort", "321");
    player = loginResult.getPlayer();
    
    // and verify she is in the place she left
    assertThat(player.getPosition(), is(newPos));
  }

  public static void shouldSeeMathildeComingInAndOutOfRoomDuringSession(Cave caveProxy, Player playerAlreadyInRoom) {
    // Log in mathilde and verify that both persons are there
    Login loginResult = caveProxy.login( "mathilde_aarskort", "321");
    Player m = loginResult.getPlayer();
    
    List<String> playersInEntryRoom = m.getPlayersHere();
    assertThat(playersInEntryRoom.size(), is(2));
    assertThat(playersInEntryRoom, hasItems(m.getName(), playerAlreadyInRoom.getName()));
    
    // log mathilde out, and in again, and verify that
    // the list of players in room is still correct
    LogoutResult logoutResult = caveProxy.logout(m.getID());
    assertThat(logoutResult, is(LogoutResult.SUCCESS));
    
    loginResult = caveProxy.login( "mathilde_aarskort", "321");
    Player m2 = loginResult.getPlayer();
    
    // DO NOT USE 'm' from here, as it will throw a session expired exception!
    assertNotNull(m2);
    // System.out.println(m2.getLongRoomDescription());
    
    playersInEntryRoom = m2.getPlayersHere();
    assertThat(playersInEntryRoom.size(), is(2));
    assertThat(playersInEntryRoom, hasItems(m2.getName(), playerAlreadyInRoom.getName()));
  }

}
