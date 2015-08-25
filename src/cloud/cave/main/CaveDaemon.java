package cloud.cave.main;

import org.slf4j.*;

import cloud.cave.config.*;
import cloud.cave.domain.Cave;
import cloud.cave.ipc.*;
import cloud.cave.server.*;

/**
 * The 'main' daemon to run on the server side. It uses a ServerFactory that
 * reads all relevant parameters to define the server side delegates
 * (subscription service, database connector, reactor implementation, IPs and
 * ports of connections...).
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class CaveDaemon {
  private static Thread daemon; 

  public static void main(String[] args) throws InterruptedException {
    
    // Create the logging
    Logger logger = LoggerFactory.getLogger(CaveDaemon.class);
 
    // Create the abstract factory to create delegates using dependency injection.
    // Here all injected delegates are defined by a set of predefined environment
    // variables.
    CaveServerFactory factory; 
    EnvironmentReaderStrategy envReader;
    envReader = new OSEnvironmentReaderStrategy();
    factory = new EnvironmentServerFactory(envReader);
    
    // Create the server side cave instance
    Cave caveServer = new StandardServerCave(factory);

    // Create the invoker on the server side, and bind it to the cave
    Invoker serverInvoker = new StandardInvoker(caveServer);
    
    // Create the server side reactor...
    Reactor reactor = factory.createReactor(serverInvoker); 

    // Make a section in the log file, marking the new session
    logger.info("=== SkyCave Reactor starting...");
    logger.info("Cave Configuration =" + caveServer.describeConfiguration());

    // Welcome 
    System.out.println("=== SkyCave Reactor ==="); 
    System.out.println("  All logging going to log file...");
    System.out.println(" Use ctrl-c to terminate!"); 
    
    // and start the daemon...
    daemon = new Thread(reactor); 
    daemon.start(); 
    
    // Ensure that its lifetime follows that of the main process
    daemon.join(); 
  }
}
