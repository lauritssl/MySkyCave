package cloud.cave.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import cloud.cave.ipc.*;
import cloud.cave.server.StandardInvoker;
import cloud.cave.server.common.ServerConfiguration;

/**
 * Test scalability of servers, what happens when a request hits a server that
 * has never handled any previous requests from the player before, because
 * the session was initialized on some other (load balanced) server
 * <p>
 * To scale from one server to several handling load, each server has to be
 * stateless, or rather handle session state in some well defined manner
 * across the entire server cluster.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestLoadBalancing {
  
  private Player player;
  private LoadBalancedLocalMethodCallClientRequestHandler crh;
  private CaveProxy caveProxy;

  @Before
  public void setup() {
    // In a multi server scenario, each server creates its own
    // cave during initialization; we mimic the multi server
    // configuration by creating two caves, assigned to different
    // servers
    Cave caveServer1 = CommonCaveTests.createTestDoubledConfiguredCave();
    Cave caveServer2 = CommonCaveTests.createTestDoubledConfiguredCave();

    // Create the server side invokers
    Invoker srh1 = new StandardInvoker(caveServer1);
    Invoker srh2 = new StandardInvoker(caveServer2);
    
    // and here comes the trick; we create a client side
    // requester that takes the two server invokers as parameters, and
    // provides a method for setting which one the
    // next requests will be forwarded to.
    crh = new LoadBalancedLocalMethodCallClientRequestHandler(srh1,srh2);
    
    // Create the cave proxy, and login mikkel; server communication
    // will be made with server 1 as this is the default for the
    // load balancing requester.
    caveProxy = new CaveProxy(crh);
    Login loginResult = caveProxy.login( "mikkel_aarskort", "123");

    player = loginResult.getPlayer();
  }

  @Test
  public void shouldProveThatOurServerIsStatefulAndThusDoesNotScale() {
    // Verify that requests are forwarded to server 1
    assertThat(crh.toString(), containsString("server 1"));
    // Verify that method calls runs smoothly to the server
    assertThat(player.getLongRoomDescription(), containsString("[0] Mikkel"));
    
    // now we simulate that the next request will be forwarded to server 2
    // by the load balancing of the SkyCave system
    crh.setWhichServerToUse(2);
    // Verify that requests are forwarded to server 2
    assertThat(crh.toString(), containsString("server 2"));
    
    // Verify that the shit hits the fan now!
    try {
      String d = player.getLongRoomDescription();
      assertThat(d, containsString("[0] Mikkel"));
    } catch(NullPointerException exc) {
      // In a proper scalable implementation, session state
      // must be available across all servers. This is 
      // future exercise. Enable the fail below once this
      // is implemented.
      // fail("The server is statefull which disallows scaling!");
    }
  }

}

/**
 * A test double request handler which simulates load balancing
 * requests over two servers, by allowing to choose which
 * of two request handlers to forward the next requests to.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
class LoadBalancedLocalMethodCallClientRequestHandler implements ClientRequestHandler {
  private Invoker requesterServer1;
  private Invoker requesterServer2;
  
  private int whichOne;
  

  public LoadBalancedLocalMethodCallClientRequestHandler(
      Invoker srh1, Invoker srh2) {
    this.requesterServer1 = srh1;
    this.requesterServer2 = srh2;
    this.whichOne = 1;
  }

  public void setWhichServerToUse(int no) {
    assert no == 1 || no == 2;
    whichOne = no;
  }
  
  @Override
  public void initialize(ServerConfiguration config) {
    // Not relevant, as this request handler is only used in
    // testing and under programmatic control
  }
  
  @Override
  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) {
    Invoker requestHandler = null;
    if (whichOne==1) requestHandler = requesterServer1; else requestHandler = requesterServer2;
    // System.out.println("--> CRH("+whichOne+"): "+ requestJson);
    JSONObject reply = requestHandler.handleRequest(requestJson);
    // System.out.println("<-- CRH("+whichOne+"): "+ reply);
    return reply;
  }

  @Override
  public String toString() {
    return "LoadBalancedLocalMethodCallClientRequestHandler, dispatching to server "+whichOne;
  }
}

