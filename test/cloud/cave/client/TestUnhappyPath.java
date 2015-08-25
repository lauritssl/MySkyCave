package cloud.cave.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;

import cloud.cave.client.CaveProxy;
import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.domain.*;
import cloud.cave.doubles.*;
import cloud.cave.ipc.*;
import cloud.cave.server.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * Testing unhappy paths, ie. scenarios where there are network problems.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class TestUnhappyPath {

  private Cave cave;
  private SaboteurCRHDecorator saboteur;

  @Before
  public void setup() {
    // Create the server tier
    Cave caveRemote = CommonCaveTests.createTestDoubledConfiguredCave();

    Invoker srh = new StandardInvoker(caveRemote);

    ClientRequestHandler properCrh = new LocalMethodCallClientRequestHandler(srh);
    
    // Decorate the proper CRH with one that simulate errors, i.e. a Saboteur
    saboteur = new SaboteurCRHDecorator(properCrh);

    cave = new CaveProxy(saboteur);
  }
  
  @Test
  public void shouldThrowIPCExceptionForTimeOut() {
    // Tell the saboteur to throw exception
    saboteur.throwNextTime("Could Not Connect to server");
    
    // One player
    @SuppressWarnings("unused")
    Login loginResult = cave.login( "mikkel_aarskort", "123");
  }
  
  // Make the server unstable internally
  @Test
  public void shouldReportOnTimeoutErrorOnSubscriptionService() {

    CaveServerFactory factory = new AllTestDoubleFactory() {
    
      public SubscriptionService createSubscriptionServiceConnector() {

        // Heavy setup to introduce errors on the server side using
        // a Meszaros Saboteur
        SubscriptionService saboteurSubscriptionService = new SubscriptionService() {
          @Override
          public SubscriptionRecord lookup(String loginName, String password) {
            throw new CaveIPCException("SubscriptionService: Timeout in connecting to the service", null);
          }
          @Override
          public void initialize(ServerConfiguration config) {
          }
          @Override
          public ServerConfiguration getConfiguration() {
            return null;
          }
          @Override
          public void disconnect() {
          }
        };
    
        return saboteurSubscriptionService;
      }
    };
    
    cave = new StandardServerCave(factory);
    
    // Try out the login, will result in a internal server error as
    // the connection to the subscription fails
    Login loginResult = cave.login( "mathilde_aarskort", "321");
    Player p2 = loginResult.getPlayer();
    assertNull(p2);
    assertThat(loginResult.getResultCode(), is(LoginResult.LOGIN_FAILED_SERVER_ERROR));
  }


}
