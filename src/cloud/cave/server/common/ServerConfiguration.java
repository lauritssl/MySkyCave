package cloud.cave.server.common;

import cloud.cave.config.*;

/**
 * Record that defines the server information for a cluster of nodes.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public class ServerConfiguration {

  private ServerData data0;
  
  /** Create a server configuration by reading an environment variable.
   * The strategy used to do the actual read is defined by the reader
   * strategy. The environment variable must be in the format 
   * "(ip0):(port0),(ip1):(port1)" i.e. comma separated lists of
   * ip addresses and ports.
   * 
   * @param environmentReader the strategy used to read the variable
   * @param environmentVariable the variable to read as a server
   * configuration, like e.g. SKYCAVE_APPSERVER. 
   */
  // TODO: LATER - ServerConfiguration only handles a single server, not a cluster
  public ServerConfiguration(EnvironmentReaderStrategy environmentReader, 
      String environmentVariable) {
    String asString = Config.failFastRead(environmentReader, environmentVariable);
    
    String[] tokens = asString.split(":");
    data0 = new StandardServerData(tokens[0], Integer.parseInt(tokens[1]));
  }
  
  /**
   * Create a server configuration directly. Should not be used except for the
   * test code.
   * 
   * @param ip
   *          the ip or hostname of the server/service
   * @param port
   *          the port number of the server/service
   */
  public ServerConfiguration(String ip, int port) {
    data0 = new StandardServerData(ip, port);
  }

  public ServerData get(int index) {
    if ( index > 0 ) { throw new RuntimeException("ServerConfiguration: get not implemented in detail yet"); }
    return data0;
  }

  @Override
  public String toString() {
    return "ServerConfiguration [serverData=" + data0 + "]";
  }
  
}

class StandardServerData implements ServerData {

  private String hostName;
  private int portNumber;

  public StandardServerData(String hostName, int portNumber) {
    this.hostName = hostName;
    this.portNumber = portNumber;
  }

  @Override
  public String getHostName() {
    return hostName;
  }

  @Override
  public int getPortNumber() {
    return portNumber;
  }

  @Override
  public String toString() {
    return hostName + ":" + portNumber;
  }
}