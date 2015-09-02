package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by Soren Lundtoft and Laurits Langberg on 02/09/15.
 */
public class StandardWeatherService implements WeatherService {

    private ServerConfiguration configuration;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) {

        //Switch case to account for different spelling on weather server
        String regionStr;
        switch (region){
            case AALBORG:
                regionStr = "Aalborg";
                break;
            case ODENSE:
                regionStr = "Odense";
                break;
            case COPENHAGEN:
                regionStr = "Copenhagen";
                break;
            default:
                regionStr = "Arhus";
        }

        try {

            HttpResponse<String> response = Unirest.get("http://" + configuration.get(0) + "/cave/weather/api/v1/{groupName}/{playerID}/{region}")
                    .routeParam("groupName", groupName)
                    .routeParam("playerID", playerID)
                    .routeParam("region", regionStr)
                    .asString();

            //Needed as Unirest gives us an org.json.JSONObject and we are working with an org.json.simple.JSONObject
            //Workaround: parse the response as a string to a simple JSON object that we can return
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
