package cloud.cave.doubles;

import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.CircuitBreakerSubscriptionService;
import cloud.cave.service.CircuitBreakerWeatherService;
import cloud.cave.service.SubscriptionService;

import javax.swing.*;

/**
 * Created by lalan on 04/09/15.
 */
public class SaboteurSubscriptionService implements SubscriptionService {

    private CircuitBreakerSubscriptionService subscriptionService;
    private ServerConfiguration configuration;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {

        SubscriptionRecord result;

        //Fix for mac OSX - default look and feel does not work.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(JOptionPane.showConfirmDialog(null, "Do you want a TimeOut?", "TimeOutMaker",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            subscriptionService.setTimeout(100, 10); //Setting timeout for faster testing
            result = subscriptionService.lookup(loginName, password);
        } else{
            subscriptionService.setTimeout(1000, 10); //Setting timeout for faster testing
            result = subscriptionService.lookup(loginName, password);
        }

        return result;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        subscriptionService = new CircuitBreakerSubscriptionService();
        subscriptionService.initialize(config);
        configuration = config;
        subscriptionService.setTimeout(1000, 10); //Setting timeout for faster testing
    }

    @Override
    public void disconnect() {
        subscriptionService.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return subscriptionService.getConfiguration();
    }
}
