package cloud.cave.client;

import cloud.cave.common.CommonCaveTests;
import cloud.cave.config.CaveServerFactory;
import cloud.cave.domain.*;
import cloud.cave.doubles.AllTestDoubleFactory;
import cloud.cave.doubles.LocalMethodCallClientRequestHandler;
import cloud.cave.doubles.SaboteurCRHDecorator;
import cloud.cave.ipc.CaveIPCException;
import cloud.cave.ipc.ClientRequestHandler;
import cloud.cave.ipc.Invoker;
import cloud.cave.server.StandardInvoker;
import cloud.cave.server.StandardServerCave;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.SubscriptionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Testing unhappy paths, ie. scenarios where there are network problems.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 */
public class TestAppserverDisconnect {

  private Cave cave;
  private SaboteurCRHDecorator saboteur;
  private ByteArrayOutputStream baos;
  private PrintStream ps;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    baos = new ByteArrayOutputStream();
    ps = new PrintStream(baos);

    // Create the server tier
    Cave caveRemote = CommonCaveTests.createTestDoubledConfiguredCave();

    Invoker srh = new StandardInvoker(caveRemote);

    ClientRequestHandler properCrh = new LocalMethodCallClientRequestHandler(srh);
    
    // Decorate the proper CRH with one that simulate errors, i.e. a Saboteur
    saboteur = new SaboteurCRHDecorator(properCrh);

    cave = new CaveProxy(saboteur);
  }
  
  @Test
  public void shouldThrowExceptionWhenServersISDone() {
    // One player
    @SuppressWarnings("unused")
    Login loginResult = cave.login( "mikkel_aarskort", "123");

    Player player = loginResult.getPlayer();

    player.move(Direction.NORTH);

    // Tell the saboteur to throw exception
    saboteur.throwNextTime("Connection Excepetion");

    thrown.expect(CaveIPCException.class);
    player.move(Direction.SOUTH);
  }


  @Test
  public void shouldReportIfServerIsUnreachable() {
    String cmdList = "n\nq\n";

    CmdInterpreter cmd = new CmdInterpreter(cave, "magnus_aarskort", "312", ps, makeToInputStream(cmdList));
    cmd.readEvalLoop();

    String output = baos.toString();

    System.out.println(output);

    assertThat(output, containsString("*** Sorry - I cannot do that as I am disconnected from the cave, please quit ***"));
  }

  private InputStream makeToInputStream(String cmdList) {
    InputStream is = new ByteArrayInputStream(cmdList.getBytes());
    return is;
  }

}
