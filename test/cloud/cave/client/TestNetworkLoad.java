package cloud.cave.client;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.ipc.*;
import cloud.cave.server.StandardInvoker;

/** A hint on how network load can be monitored using
 * a spy - without any real networking occuring.
 * <p>
 * These tests will likely fail if you change any
 * marshaling code!
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class TestNetworkLoad {

  private PlayerProxy player;
  private LoadSpyClientRequestHandler spy;

  @Before
  public void setUp() throws Exception {
    // Create the server tier
    Cave cave = CommonCaveTests.createTestDoubledConfiguredCave();
    
    // create the invoker on the server side, bind it to the cave
    Invoker srh = new StandardInvoker(cave);
    
    // create the client request handler as a test double that
    // simply uses method calls to call the 'server side'
    ClientRequestHandler crh = new LocalMethodCallClientRequestHandler(srh);
    spy = new LoadSpyClientRequestHandler(crh);
    
    // Create the cave proxy, and login mikkel
    Cave caveProxy = new CaveProxy(spy);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");
    
    player = (PlayerProxy) loginResult.getPlayer();
  }

  @Test
  public void shouldVerifyBytesSentOverNetwork() {
    spy.reset();
    player.getLongRoomDescription();
    // Verify the amount of bytes sent and received
    assertThat( spy.getSent(), is(156));
    assertThat( spy.getReived(), is(232));
  }

}
