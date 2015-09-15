package cloud.cave.doubles;

import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.SubscriptionService;

/**
 * Created by lalan on 15/09/15.
 */
public class SaboteurSubscriptionServiceAutomated implements SubscriptionService {
    private ServerConfiguration config;
    private boolean timeout;
    private SubscriptionService ss;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        if(timeout){
            throw new CaveTimeOutException("TIME_OUT");
        }else {
            ss.initialize(config);
            return ss.lookup(loginName, password);
        }
    }

    @Override
    public void initialize(ServerConfiguration config) {
        ss = new TestStubSubscriptionService();
        this.config = config;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return config;
    }

    public void throwTimeout(boolean timeout){
        this.timeout = timeout;
    }
}
