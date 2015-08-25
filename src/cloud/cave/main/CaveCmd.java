package cloud.cave.main;

import java.io.*;

import cloud.cave.client.*;
import cloud.cave.config.*;
import cloud.cave.domain.*;
import cloud.cave.ipc.*;

/**
 * Main method for a command line client. It is configured
 * through environment variables.
 * 
 * @see Config
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class CaveCmd {
  public static void main(String[] args) throws IOException {
    CaveClientFactory factory;
    EnvironmentReaderStrategy envReader;
    envReader = new OSEnvironmentReaderStrategy();
    factory = new EnvironmentClientFactory(envReader);
    
    ClientRequestHandler requestHandler = factory.createClientRequestHandler();
    Cave cave = new CaveProxy(requestHandler);
    
    String loginName = args[0];
    String pwd = args[1];

    new CmdInterpreter(cave, loginName, pwd, 
        System.out, System.in).readEvalLoop();
  }
}

