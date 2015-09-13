package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.ServerConfiguration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Created by Soren Lundtoft and Laurits Langberg on 02/09/15.
 */
public class StandardWeatherService implements WeatherService {

    private ServerConfiguration configuration;

    @Override
    public void initialize(ServerConfiguration config) {
        configuration = config;
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) throws CaveTimeOutException{

        HttpResponse<String> response = null;
        try {
            response = Unirest.get("http://" + configuration.get(0) + "/cave/weather/api/v1/{groupName}/{playerID}/{region}")
                    .routeParam("groupName", groupName)
                    .routeParam("playerID", playerID)
                    .routeParam("region", convertRegionFormat(region))
                    .asString();

            //Needed as Unirest gives us an org.json.JSONObject and we are working with an org.json.simple.JSONObject
            //Workaround: parse the response as a string to a simple JSON object that we can return
            JSONParser parser = new JSONParser();
            JSONObject result = (JSONObject) parser.parse(response.getBody());
            return result;

        } catch (UnirestException e) {
            if (e.getMessage().contains("Timeout")){
                throw new CaveTimeOutException("TIME_OUT_CLOSED_CIRCUIT", e);
            }else{
                e.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public void disconnect() {

    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Converting region to format used by weather service
     */
    public String convertRegionFormat(Region region){
        switch (region){
            case AALBORG:
                return "Aalborg";
            case ODENSE:
                return "Odense";
            case COPENHAGEN:
                return "Copenhagen";
            default:
                return "Arhus";
        }
    }
}
