package cloud.cave.doubles;

import org.json.simple.JSONObject;

import cloud.cave.ipc.CaveIPCException;
import cloud.cave.ipc.ClientRequestHandler;
import cloud.cave.server.common.ServerConfiguration;

public class LoadSpyClientRequestHandler implements ClientRequestHandler {
  private ClientRequestHandler decoratee;
  private int bytesSent;
  private int bytesReceived;

  public LoadSpyClientRequestHandler(ClientRequestHandler crh) {
    decoratee = crh;
    reset();
  }

  public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson)
      throws CaveIPCException {
    bytesSent = requestJson.toJSONString().length();
    // System.out.println("SPY: --> # "+bytesSent);
    JSONObject reply = decoratee.sendRequestAndBlockUntilReply(requestJson);
    bytesReceived = reply.toJSONString().length();
    // System.out.println("SPY: --> # "+bytesReceived);
    return reply;
  }

  public void initialize(ServerConfiguration config) {
    decoratee.initialize(config);
  }

  public void reset() {
    bytesSent = bytesReceived = 0;
  }

  public int getSent() {
    return bytesSent;
  }

  public int getReived() {
    return bytesReceived;
  }
}
