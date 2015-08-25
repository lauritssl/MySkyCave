package cloud.cave.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.Cave;
import cloud.cave.domain.Login;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import cloud.cave.ipc.Invoker;
import cloud.cave.server.StandardInvoker;

/** Testing the weather method on the
 * client side
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public class TestWeatherClient {

  private LocalMethodCallClientRequestHandler crh;
  private CaveProxy caveProxy;
  private PlayerProxy player;

  @Before
  public void setUp() throws Exception {
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
  public void shouldGetWeatherClientSide() {
    String weather = player.getWeather();
    
    // TODO: Exercise - solve the 'weather-client' exercise
    assertThat(weather, containsString("NOT IMPLEMENTED YET"));
  
    /**
    assertThat(weather, containsString("The weather in AARHUS is Clear, temperature 27.4C (feelslike -2.7C). Wind: 1.2 m/s, direction West."));
    assertThat(weather, containsString("This report is dated: Thu, 05 Mar 2015 09:38:37 +0100"));
    */
  }

}
