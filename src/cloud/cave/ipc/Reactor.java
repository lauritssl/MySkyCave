package cloud.cave.ipc;

import cloud.cave.server.common.ServerConfiguration;

/**
 * A common interface for the Reactor Role (Pattern-oriented Software
 * Architecture, vol 4, p 259)
 * <p>
 * As a reactor instance is created dynamically by the ServerFactory there
 * cannot be any other constructors than the default one. Be SURE to invoke the
 * 'initialize' method BEFORE the run() method is invoked.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 *
 */
public interface Reactor extends Runnable {

  /**
   * Initialize the reactor with the server configuration. This HAS to be
   * executed BEFORE the run() method is invoked.
   * 
   * @param invoker
   *          the server invoker to handle the dispatching of incoming
   *          requests
   * 
   * @param config
   *          the configuration of IP and ports.
   */
  void initialize(Invoker invoker, ServerConfiguration config);
}
