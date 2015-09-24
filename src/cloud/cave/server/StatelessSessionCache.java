package cloud.cave.server;

import cloud.cave.domain.Player;
import cloud.cave.service.CaveStorage;
import cloud.cave.service.WeatherService;

/**
 * Created by Soren and Laurits on 23/09/15.
 */
public class StatelessSessionCache implements PlayerSessionCache {

    private final CaveStorage storage;
    private final WeatherService weatherService;
    private final PlayerSessionCache playerSessionCache;

    public StatelessSessionCache(CaveStorage storage, WeatherService weatherService, PlayerSessionCache playerSessionCache){
        this.storage = storage;
        this.weatherService = weatherService;
        this.playerSessionCache = playerSessionCache;
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
