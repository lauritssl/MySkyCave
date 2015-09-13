package cloud.cave.doubles;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CircuitBreakerWeatherService;
import cloud.cave.service.StandardWeatherService;
import cloud.cave.service.WeatherService;
import org.json.simple.JSONObject;

import javax.swing.*;

/**
 * Created by lalan on 04/09/15.
 */
public class SaboteurWeatherService implements WeatherService {

    private CircuitBreakerWeatherService weatherService;
    private ServerConfiguration weatherConfig;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {

        JSONObject result = new JSONObject();

        //Fix for mac OSX - default look and feel does not work.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(JOptionPane.showConfirmDialog(null, "Do you want a TimeOut?", "TimeOutMaker",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            weatherService.initialize(new ServerConfiguration("caveweather.baerbak.com", 8184));
            weatherService.setTimeout(1, 10); //Setting timeout for faster testing
            result = weatherService.requestWeather(groupName, playerID, region);
        } else{
            weatherService.initialize(weatherConfig);
            weatherService.setTimeout(1, 10); //Setting timeout for faster testing
            result = weatherService.requestWeather(groupName, playerID, region);
        }

        return result;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        weatherService = new CircuitBreakerWeatherService();
        weatherService.initialize(config);
        weatherConfig = config;
        weatherService.setTimeout(1, 10); //Setting timeout for faster testing
    }

    @Override
    public void disconnect() {
        weatherService.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return weatherService.getConfiguration();
    }
}
