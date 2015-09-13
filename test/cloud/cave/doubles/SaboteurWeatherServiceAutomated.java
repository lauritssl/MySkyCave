package cloud.cave.doubles;

import cloud.cave.domain.Region;
import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CircuitBreakerWeatherService;
import cloud.cave.service.WeatherService;
import org.json.simple.JSONObject;

/**
 * Created by lalan on 13/09/15.
 */
public class SaboteurWeatherServiceAutomated implements WeatherService{
    private CircuitBreakerWeatherService ws;
    private ServerConfiguration config;
    private int timeout, waitTime;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) throws CaveTimeOutException{
        ws.initialize(config);
        ws.setTimeout(timeout, waitTime);
        return ws.requestWeather(groupName, playerID, region);
    }

    @Override
    public void initialize(ServerConfiguration config) {
        this.config = config;
        ws = new CircuitBreakerWeatherService();
        ws.initialize(config);
    }

    @Override
    public void disconnect() {
        ws.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return config;
    }

    public void setConfig(ServerConfiguration config){
        this.config = config;
    }

    public void setTimeout(int timeout, int waitTime){
        this.timeout = timeout;
        this.waitTime = waitTime;
    }
}
