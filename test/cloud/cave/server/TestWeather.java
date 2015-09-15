package cloud.cave.server;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import cloud.cave.doubles.SaboteurWeatherServiceAutomated;
import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.StandardWeatherService;
import org.json.simple.JSONObject;
import org.junit.*;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.domain.*;
import cloud.cave.doubles.TestStubWeatherService;
import cloud.cave.service.WeatherService;

/**
 * TDD Implementation of the weather stuff - initial steps.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class TestWeather {

  private Cave cave;
  private String loginName;
  private Player player;

  @Before
  public void setUp() throws Exception {
    cave = CommonCaveTests.createTestDoubledConfiguredCave();
    loginName = "mikkel_aarskort";
    Login loginResult = cave.login(loginName, "123");
    player = loginResult.getPlayer();
  }


  @Test
  public void shouldGetWeatherServerSide() {
    String weather = player.getWeather();
    assertThat(weather, containsString("The weather in AARHUS is Clear, temperature 27.4C (feelslike -2.7C). Wind: 1.2 m/s, direction West."));
    assertThat(weather, containsString("This report is dated: Thu, 05 Mar 2015 09:38:37 +0100"));
  }


  @Test
  public void shouldRejectUnknownPlayer() {
    // Test the raw weather service api for unknown players
    WeatherService ws = new TestStubWeatherService();
    JSONObject json = ws.requestWeather("grp02", "user-003", Region.COPENHAGEN);
    assertThat(json.get("authenticated").toString(), is("false"));
    assertThat(json.get("errorMessage").toString(), is("GroupName grp02 or playerID user-003 is not authenticated"));
    
    // Try it using the full api
    Login loginResult = cave.login("mathilde_aarskort", "321");
    player = loginResult.getPlayer();
    assertNotNull("The player should have been logged in", player);
    
    String weather = player.getWeather();
    assertThat(weather, containsString("The weather service failed with message: GroupName grp02 or playerID user-003 is not authenticated"));
  }

  @Ignore
  @Test
  public void shouldOnlyTimeoutWhenCallingTheSlowWeatherService(){
    SaboteurWeatherServiceAutomated ws = new SaboteurWeatherServiceAutomated();
    ws.setTimeout(1,3);
    Exception exception = null;

    try{
      //Real server
      ws.initialize(new ServerConfiguration("caveweather.baerbak.com", 8182));
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      exception = e;
    }
    assertNull(exception);


    try{
      //Slow server
      ws.setConfig(new ServerConfiguration("caveweather.baerbak.com", 8184));
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      exception = e;
    }
    assertEquals("That the exception is a CaveTimeOutException:", exception.getClass(), CaveTimeOutException.class);

  }

  @Ignore
  @Test
  public void shouldBeOpenCircutAfter3Try(){
    SaboteurWeatherServiceAutomated ws = new SaboteurWeatherServiceAutomated();

    //Slow server
    ws.initialize(new ServerConfiguration("caveweather.baerbak.com", 8184));
    ws.setTimeout(1,3);

    try {
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      assertEquals("The Circut should be open", e.getMessage(), "TIME_OUT_CLOSED_CIRCUIT");
    }

    try {
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      assertEquals("The Circut should be open", e.getMessage(), "TIME_OUT_CLOSED_CIRCUIT");
    }

    try {
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      assertEquals("The Circut should be open", e.getMessage(), "TIME_OUT_CLOSED_CIRCUIT");
    }

    try {
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      assertEquals("The Circut should be open", e.getMessage(), "TIME_OUT_OPEN_CIRCUIT");
    }

    try {
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      assertEquals("The Circut should be open", e.getMessage(), "TIME_OUT_OPEN_CIRCUIT");
    }

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      assertEquals("The Circut should be open", e.getMessage(), "TIME_OUT_OPEN_CIRCUIT");
    }

    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Exception exception = null;

    try{
      //Real server
      ws.setConfig(new ServerConfiguration("caveweather.baerbak.com", 8182));
      ws.requestWeather("ITS1", "55e41406e4b067dd3c8fa522", Region.AARHUS);
    }catch (CaveTimeOutException e){
      exception = e;
    }
    assertNull(exception);

  }

  @Test
  public void serverConfigurationShouldBeTheSame(){
    ServerConfiguration sv = new ServerConfiguration("caveweather.baerbak.com", 8182);

    StandardWeatherService ws = new StandardWeatherService();

    ws.initialize(sv);

    assertEquals("Server configuration is the same: ", sv, ws.getConfiguration());
  }

  @Test
  public void regionStringIsConvertedCorrectly(){
    StandardWeatherService ws = new StandardWeatherService();

    assertEquals("Arhus should be equal to Region.AARHUS", "Arhus", ws.convertRegionFormat(Region.AARHUS));

    assertEquals("Aalborg should be equal to Region.AALBORG", "Aalborg", ws.convertRegionFormat(Region.AALBORG));

    assertEquals("Copenhagen should be equal to Region.COPENHAGEN", "Copenhagen", ws.convertRegionFormat(Region.COPENHAGEN));

    assertEquals("Odense should be equal to Region.ODENSE", "Odense", ws.convertRegionFormat(Region.ODENSE));
  }

}
