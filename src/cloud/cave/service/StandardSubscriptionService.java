package cloud.cave.service;

import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

/**
 * Created by Søren Lundtoft & Laurits Langberg on 02/09/15.
 */
public class StandardSubscriptionService implements SubscriptionService {

    private ServerConfiguration configuration;

    @Override
    public SubscriptionRecord lookup(String loginName, String password) {

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
