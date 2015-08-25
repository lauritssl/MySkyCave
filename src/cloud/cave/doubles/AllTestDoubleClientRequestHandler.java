package cloud.cave.doubles;

import org.json.simple.JSONObject;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.domain.Cave;
import cloud.cave.ipc.*;
import cloud.cave.server.*;
import cloud.cave.server.common.ServerConfiguration;

/**
 * A client request handler that is pre-configured to abstract
 * a server completely away, and replace everything by fake objects
 * and test doubles. Is used in the Cmd tool.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class AllTestDoubleClientRequestHandler implements
    ClientRequestHandler {

  private Invoker invoker;

  public AllTestDoubleClientRequestHandler() {
    invoker = null;
  }

  @Override
  public void initialize(ServerConfiguration config) {
    CaveServerFactory factory = new AllTestDoubleFactory();

    Cave cave = new StandardServerCave(factory);
    invoker = new StandardInvoker(cave);
  }
  
  @Override
  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) {
    // System.out.println("--> CRH: "+ requestJson);
    JSONObject reply = invoker.handleRequest(requestJson);
    // System.out.println("<-- CRH: "+ reply);
    return reply;
  }

  public String toString() {
    return "TestDoubleClientRequestHandler: Configured with pure test doubles for all server side abstractions.";
  }

}
