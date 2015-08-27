package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import java.util.List;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;

/**
 * Initial template of TDD of students' exercises
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestWall {

  private Cave cave;
  
  private Player player;

  @Before
  public void setUp() throws Exception {
    cave = CommonCaveTests.createTestDoubledConfiguredCave();

    Login loginResult = cave.login( "mikkel_aarskort", "123");
    player = loginResult.getPlayer();
  }

  @Test
  public void shouldWriteToAndReadWall() {
    String message = "This is message no. 1";
    player.addMessage(message);
    List<String> wallContents = player.getMessageList();

    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString(message));
  }

  @Test
  public void shouldShowPlayernameOnWall() {
    player.addMessage("Test");
    List<String> wallContents = player.getMessageList();
    assertThat( wallContents.get(0), containsString("[" + player.getName() + "]"));
  }

}
