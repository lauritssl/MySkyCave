package cloud.cave.service;

import cloud.cave.domain.Direction;
import cloud.cave.ipc.CaveTimeOutException;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;

import java.util.List;

/**
 * Created by Soren and Laurits on 22/09/15.
 */
public class FailsafeMongoStorage implements CaveStorage {
    private CaveStorage mongo;

    @Override
    public RoomRecord getRoom(String positionString) {
        return mongo.getRoom(positionString);
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        try {
            return mongo.addRoom(positionString, description);
        }catch (MongoException e){
            throw new CaveTimeOutException("MONGO_EXCEPTION", e);
        }
    }

    @Override
    public void addMessage(String positionString, String message) {
        mongo.addMessage(positionString, message);
    }

    @Override
    public List<String> getMessageList(String positionString) {
        return mongo.getMessageList(positionString);
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        return mongo.getSetOfExitsFromRoom(positionString);
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        return mongo.getPlayerByID(playerID);
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        mongo.updatePlayerRecord(record);
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString, int from, int to) {
        return mongo.computeListOfPlayersAt(positionString, from, to);
    }

    @Override
    public int computeCountOfActivePlayers() {
        return mongo.computeCountOfActivePlayers();
    }

    @Override
    public void initialize(ServerConfiguration config) {
        mongo = new MongoStorage();
        mongo.initialize(config);
    }

    @Override
    public void disconnect() {
        mongo.disconnect();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return mongo.getConfiguration();
    }
}
