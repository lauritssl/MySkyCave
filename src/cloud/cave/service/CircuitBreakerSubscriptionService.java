package cloud.cave.service;

import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Created by lundtoft on 13/09/15.
 */
public class CircuitBreakerSubscriptionService implements SubscriptionService {

    private SubscriptionService ss;
    private int timeOutTry = 0;
    private LocalDateTime subscriptionOpenCircuitStartTime;
    private static Logger logger = LoggerFactory.getLogger(StandardWeatherService.class);
    private int waitTime;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {
        LocalDateTime now = LocalDateTime.now();
        String errMsg;

        if(timeOutTry < 3){

            try {
                SubscriptionRecord result = ss.lookup(loginName, password);
                timeOutTry = 0;
                return result;
            } catch (CaveTimeOutException e) {
                timeOutTry++;
                errMsg = "Subscription service timed out (closed circuit). Attempts: "+timeOutTry;
                logger.error(errMsg);
                System.out.println(errMsg);
                throw new CaveTimeOutException("TIME_OUT_CLOSED_CIRCUIT", e);
            }
        }else if(timeOutTry == 3){
            subscriptionOpenCircuitStartTime = LocalDateTime.now();
            errMsg = "Subscription service in open circuit.";
            logger.error(errMsg);
            System.out.println(errMsg);
            timeOutTry++;
            throw new CaveTimeOutException("TIME_OUT_OPEN_CIRCUIT");
        }else {
            if (now.isAfter(subscriptionOpenCircuitStartTime.plusSeconds(waitTime))) {
                timeOutTry = 2; //half-open circuit
                errMsg = "Subscription service in half-open circuit. Attempting again...";
                logger.error(errMsg);
                System.out.println(errMsg);
                return lookup(loginName, password);
            } else {
                errMsg = "Subscription service in open circuit.";
                logger.error(errMsg);
                System.out.println(errMsg);
                throw new CaveTimeOutException("TIME_OUT_OPEN_CIRCUIT");
            }
        }
    }

    @Override
    public void initialize(ServerConfiguration config) {
        ss = new StandardSubscriptionService();
        ss.initialize(config);
        setTimeout(5, 60);
    }

    @Override
    public void disconnect() {
        ss.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return ss.getConfiguration();
    }

    public void setTimeout(int timeout, int waitTime){
        this.waitTime = waitTime;

        Unirest.setTimeouts(timeout, timeout);
    }

    public void setSubscriptionService(SubscriptionService ss){
        this.ss = ss;
        ss.initialize(ss.getConfiguration());
    }
}
