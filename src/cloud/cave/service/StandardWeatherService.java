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
    private int timeOutTry = 0;
    private LocalDateTime weatherOpenCircuitStartTime;
    private static Logger logger = LoggerFactory.getLogger(StandardWeatherService.class);

    @Override
    public void initialize(ServerConfiguration config) {
        configuration = config;
        Unirest.setTimeouts(5000, 5000);
    }

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) throws CaveTimeOutException{

        LocalDateTime now = LocalDateTime.now();
        String errMsg;

        if(timeOutTry < 3){
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
                timeOutTry = 0;
                return result;
            } catch (UnirestException e) {
                if (e.getMessage().contains("Timeout")){
                    timeOutTry++;
                    errMsg = "Weather service timed out (closed circuit). Try: "+timeOutTry;
                    logger.error(errMsg);
                    System.out.println(errMsg);
                    throw new CaveTimeOutException("TIME_OUT_CLOSED_CIRCUIT", e);
                }else{
                    e.printStackTrace();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else if(timeOutTry == 3){
            weatherOpenCircuitStartTime = LocalDateTime.now();
            errMsg = "Weather service in open circuit.";
            logger.error(errMsg);
            System.out.println(errMsg);
            timeOutTry++;
            throw new CaveTimeOutException("TIME_OUT_OPEN_CIRCUIT");
        }else {
            if (now.isAfter(weatherOpenCircuitStartTime.plusSeconds(60))) {
                timeOutTry = 2; //half-open circuit
                errMsg = "Weather service in half-open circuit. Attempting again...";
                logger.error(errMsg);
                System.out.println(errMsg);
                return requestWeather(groupName, playerID, region);
            } else {
                errMsg = "Weather service in open circuit.";
                logger.error(errMsg);
                System.out.println(errMsg);
                throw new CaveTimeOutException("TIME_OUT_OPEN_CIRCUIT");
            }
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
}
