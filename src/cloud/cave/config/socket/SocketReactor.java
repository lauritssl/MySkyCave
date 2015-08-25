package cloud.cave.config.socket;

import java.io.*;
import java.net.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.slf4j.*;

import cloud.cave.ipc.*;
import cloud.cave.server.common.ServerConfiguration;

/**
 * The Reactor that binds to the OS and forwards any incoming requests on a
 * server socket to the given server invoker.
 * <p>
 * The connection style is similar to HTTP/1.0 in that a separate
 * connection is made for every request. This is very simple
 * and easy to implement but gives a lot of connection overhead.
 * <p>
 * As this reactor is meant as a case study, it is abnormally
 * talkative for a server and prints a lot on the console
 * instead of logging stuff.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class SocketReactor implements Reactor {
  private int portNumber;
  private Invoker invoker;
  private Logger logger;
  private JSONParser parser;

  private ServerSocket serverSocket = null;

  @Override
  public void initialize(Invoker invoker, ServerConfiguration config) {
    portNumber = config.get(0).getPortNumber();
    this.invoker = invoker;
    parser = new JSONParser();
    logger = LoggerFactory.getLogger(SocketReactor.class);
  }

  @Override
  public void run() {
    openServerSocket();

    System.out.println("*** Server socket established ***");
    
    boolean isStopped = false;
    while (!isStopped) {

      System.out.println("--> Accepting...");
      Socket clientSocket = null;
      try {
        clientSocket = serverSocket.accept();
      } catch(IOException e) {
        if(isStopped) {
          System.out.println("Server Stopped.") ;
          return;
        }
        throw new RuntimeException(
            "Error accepting client connection", e);
      }
      System.out.println("--> AcceptED!");

      try {
        readMessageAndDispatch(clientSocket);
      } catch (IOException e) {
        logger.error("IOException during readMessageAndDispatch", e);
        System.out.println("ERROR: IOException encountered, review log");
      }
    }
    System.out.println("Server Stopped.");
  }

  private void readMessageAndDispatch(Socket clientSocket) throws IOException {
    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    BufferedReader in = new BufferedReader(new InputStreamReader(
        clientSocket.getInputStream()));

    String inputLine;
    inputLine = in.readLine();
    System.out.println("--> Received " + inputLine);
    
    JSONObject requestJson = null, reply = null;
    try {
      requestJson = (JSONObject) parser.parse(inputLine);
      reply = invoker.handleRequest(requestJson);
      System.out.println("--< replied: " + reply);
    } catch (ParseException e) {
      String errorMsg = "JSON Parse error on input = " + inputLine;
      logger.error(errorMsg, e);
      reply = Marshaling.createInvalidReplyWithExplantion(
          StatusCode.SERVER_FAILURE, errorMsg);
      System.out.println("--< !!! replied: "+reply);
    }
    out.println(reply.toString());

    System.out.println("Closing socket...");
    in.close();
    out.close();
  }

  private void openServerSocket() {
    try {
      this.serverSocket = new ServerSocket(this.portNumber);
      System.out.println("Socket accepting on port: "+portNumber);
    } catch (IOException e) {
      logger.error("Cannot open port "+portNumber, e);
      System.out.println("Failed to open server socket at port "+portNumber);
      System.exit(-1);
    } 
  }
  
  public String toString() {
    return "SocketReactor. Assigned to port: "+portNumber;
  }

}