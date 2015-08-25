package cloud.cave.command;

import org.junit.Before;
import org.junit.Test;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;

/** TDD the Command pattern.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class TestNewCommand {

  private Player player;
  
  @Before
  public void setup() {
    Cave cave = CommonCaveTests.createTestDoubledConfiguredCave();

    Login loginResult = cave.login( "mikkel_aarskort", "123");
    player = loginResult.getPlayer();
  }

  @Test
  public void shouldExecuteHomeCommand() {
    Common.shouldExecuteHomeCommand(player);
  }

  @Test
  public void shouldExecuteJumpCommand() {
    Common.shouldExecuteJumpCommand(player);
  }

  @Test
  public void shouldNotExecuteUnknownCommand() {
    Common.shouldExecuteUnknownCommand(player);
  }
}
