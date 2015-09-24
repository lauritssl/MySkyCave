package cloud.cave.server;

import cloud.cave.domain.Player;
import cloud.cave.domain.Region;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.server.common.SubscriptionRecord;
import cloud.cave.service.CaveStorage;
import cloud.cave.service.WeatherService;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Soren and Laurits on 23/09/15.
 */
public class StatelessSessionCache implements PlayerSessionCache {
    private MongoCollection<Document> cache;
    private final CaveStorage storage;
    private final WeatherService weatherService;
    private final PlayerSessionCache playerSessionCache;

    public StatelessSessionCache(CaveStorage storage, WeatherService weatherService, PlayerSessionCache playerSessionCache){
        this.storage = storage;
        this.weatherService = weatherService;
        this.playerSessionCache = playerSessionCache;
<<<<<<< Updated upstream

        ServerConfiguration config = storage.getConfiguration();

        ServerAddress[] mongoServers = new ServerAddress[config.size()];

        for (int i = 0; i < config.size(); i++) {
            mongoServers[i] = new ServerAddress(config.get(i).getHostName(), config.get(i).getPortNumber());
        }

        MongoClient mongo = new MongoClient(Arrays.asList(mongoServers));
        MongoDatabase db = mongo.getDatabase("cave");
        cache = db.getCollection("cache");
=======
>>>>>>> Stashed changes
    }

    @Override
    public Player get(String playerID) {
        String pID = storage.sessionGet(playerID);

        if(pID != null){
           return new StandardServerPlayer(playerID, storage, weatherService, playerSessionCache);
        }
        return null;
    }

    @Override
    public void add(String playerID, Player player) {
        storage.sessionAdd(playerID, player);
    }

    @Override
    public void remove(String playerID) {
        storage.sessionRemove(playerID);
    }

}
