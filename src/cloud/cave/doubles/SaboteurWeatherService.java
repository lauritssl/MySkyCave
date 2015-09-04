package cloud.cave.doubles;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.StandardWeatherService;
import cloud.cave.service.WeatherService;
import org.json.simple.JSONObject;

import javax.swing.*;

/**
 * Created by lalan on 04/09/15.
 */
public class SaboteurWeatherService implements WeatherService {

    private StandardWeatherService weatherService;

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
            result.put("authenticated", "false");
            result.put("errorMessage", "SERVER_TIMEOUT");
        } else{
            result = weatherService.requestWeather(groupName, playerID, region);
        }

        return result;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        weatherService = new StandardWeatherService();
        weatherService.initialize(config);
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
