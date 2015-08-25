package cloud.cave.ipc;

import org.json.simple.JSONObject;

/** An Invoker pattern (Pattern-oriented Software Architecture, vol 4, p 244)
 * that encapsulate dispatch of request messages to the proper methods
 * of objects on the server side. Note that in our implementation, the
 * reception of messages is not made by the Invoker, but by the Reactor.
 * <p>
 * Also compared to the POSA description, the invoker has the 
 * added responsibility to parse a JSON request object to retrieve
 * the parameters (object id, operation name, arguments), and 
 * also to do the event dispatching of the Reactor pattern. 
 * 
 * @see Reactor
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 *
 */
public interface Invoker {

  /**
   * This method is called by the server daemon/reactor on the server side that receives raw
   * byte chuncks from clients, converts them into JSON and then lets an instance of
   * Invoker interpret and makes the proper invocation of a method on the
   * proper server side object. Essentially the method encapsulate the
   * '## Dispatch the event.' section of the Reactor pattern (POSA, vol 4, p 259)
   * 
   * @param requestJson
   *          the request object from the client
   * @return the returned answer from the proper server-side object
   */
  JSONObject handleRequest(JSONObject requestJson);

}
