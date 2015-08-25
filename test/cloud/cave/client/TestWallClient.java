package cloud.cave.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import cloud.cave.ipc.Invoker;
import cloud.cave.server.StandardInvoker;

/**
 * Testing of the wall behavior on the client side.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */

public class TestWallClient {

  private Player player;
  private LocalMethodCallClientRequestHandler crh;
  private CaveProxy caveProxy;

  @Before
  public void setup() {
    // Create the server tier
    Cave cave = CommonCaveTests.createTestDoubledConfiguredCave();
    
    // create the invoker on the server side, bind it to the cave
    Invoker srh = new StandardInvoker(cave);
    
    // create the client request handler as a test double that
    // simply uses method calls to call the 'server side'
    crh = new LocalMethodCallClientRequestHandler(srh);
    
    // Create the cave proxy, and login mikkel
    caveProxy = new CaveProxy(crh);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");
    
    player = (PlayerProxy) loginResult.getPlayer();

  }

  @Test
  public void shouldWriteToAndReadWall() {
    player.addMessage("This is message no. 1");
    List<String> wallContents = player.getMessageList();
    assertThat( wallContents.size(), is(1));
    assertThat( wallContents.get(0), containsString("NOT IMPLEMENTED YET"));
  }

}
