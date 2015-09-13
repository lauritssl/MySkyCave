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

import java.time.LocalDateTime;

/**
 * Created by lundtoft on 13/09/15.
 */
public class CircuitBreakerWeatherService implements WeatherService {

    private WeatherService ws;
    private int timeOutTry = 0;
    private LocalDateTime weatherOpenCircuitStartTime;
    private static Logger logger = LoggerFactory.getLogger(StandardWeatherService.class);
    private int waitTime;

    @Override
    public JSONObject requestWeather(String groupName, String playerID, Region region) throws CaveTimeOutException{
        LocalDateTime now = LocalDateTime.now();
        String errMsg;

        if(timeOutTry < 3){

            try {
                JSONObject result = ws.requestWeather(groupName, playerID, region);
                timeOutTry = 0;
                return result;
            } catch (CaveTimeOutException e) {
                timeOutTry++;
                errMsg = "Weather service timed out (closed circuit). Try: "+timeOutTry;
                logger.error(errMsg);
                System.out.println(errMsg);
                throw new CaveTimeOutException("TIME_OUT_CLOSED_CIRCUIT", e);
            }
        }else if(timeOutTry == 3){
            weatherOpenCircuitStartTime = LocalDateTime.now();
            errMsg = "Weather service in open circuit.";
            logger.error(errMsg);
            System.out.println(errMsg);
            timeOutTry++;
            throw new CaveTimeOutException("TIME_OUT_OPEN_CIRCUIT");
        }else {
            if (now.isAfter(weatherOpenCircuitStartTime.plusSeconds(waitTime))) {
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
    }

    @Override
    public void initialize(ServerConfiguration config) {
        ws = new StandardWeatherService();
        ws.initialize(config);
        setTimeout(5, 60);
    }

    @Override
    public void disconnect() {
        ws.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return ws.getConfiguration();
    }

    public void setTimeout(int timeout, int waitTime){
        this.waitTime = waitTime;

        timeout = timeout * 1000;
        Unirest.setTimeouts(timeout, timeout);
    }
}
