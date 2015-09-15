package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Created by Soren Lundtoft & Laurits Langberg on 02/09/15.
 */
public class StandardSubscriptionService implements SubscriptionService {

    private ServerConfiguration configuration;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) throws CaveTimeOutException{

        try {

            HttpResponse<JsonNode> response = Unirest.get("http://" + configuration.get(0) + "/api/v1/auth?loginName={loginName}&password={password}")
                    .routeParam("loginName", loginName)
                    .routeParam("password", password)
                    .asJson();

            if(response.getBody().getObject().getBoolean("success")) {

                JSONObject result = response.getBody().getObject().getJSONObject("subscription");

                return new SubscriptionRecord(result.getString("playerID"), result.getString("playerName"), result.getString("groupName"), Region.valueOf(result.getString("region")));
            }

        } catch (UnirestException e) {
            if (e.getMessage().contains("Timeout")){
                throw new CaveTimeOutException("TIME_OUT_CLOSED_CIRCUIT", e);
            }else{
                e.printStackTrace();
            }
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
