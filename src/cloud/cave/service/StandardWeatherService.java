package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by lalan on 02/09/15.
 */
public class StandardWeatherService implements WeatherService {

    private ServerConfiguration configuration;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        try {

            HttpResponse<String> response = Unirest.get("http://" + configuration.get(0) + "/cave/weather/api/v1/{groupName}/{playerID}/{region}")
                    .routeParam("groupName", groupName)
                    .routeParam("playerID", playerID)
                    .routeParam("region", "Arhus")
                    .asString();

            JSONParser parser = new JSONParser();
            JSONObject result = (JSONObject) parser.parse(response.getBody());

            return result;

        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        configuration = config;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
