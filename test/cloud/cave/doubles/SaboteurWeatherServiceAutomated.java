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

    private WeatherService ws;
    private boolean timeout = false;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) throws CaveTimeOutException{

        if(timeout) {
            //Throwing fake timeout
            throw new CaveTimeOutException("TIME_OUT");
        } else{
            return ws.requestWeather(groupName, playerID, region);
        }
    }

    @Override
    public void initialize(ServerConfiguration config) {
        ws = new TestStubWeatherService();
        ws.initialize(config);
    }

    @Override
    public void disconnect() {
        ws.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return ws.getConfiguration();
    }

    public void throwTimeout(boolean timeout){
        this.timeout = timeout;
    }
}
