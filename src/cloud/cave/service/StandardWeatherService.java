package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONObject;

/**
 * Created by lalan on 02/09/15.
 */
public class StandardWeatherService implements WeatherService {

    private ServerConfiguration configuration;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {
        try {

            HttpResponse<JsonNode> response = Unirest.get("http://" + FIND_REFRRENCE_TO_URL + "/cave/weather/api/v1/{groupName}/{playerID}/{region}")
                    .routeParam("groupName", groupName)
                    .routeParam("playerID", playerID)
                    .routeParam("region", region.toString())
                    .asJson();

            JSONObject result = response.getBody().getObject();

            return result;

        } catch (UnirestException e) {
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
