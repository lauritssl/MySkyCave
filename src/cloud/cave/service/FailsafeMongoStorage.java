package cloud.cave.service;

import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.ipc.CaveStorageException;
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
        try {
            return mongo.getRoom(positionString);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        try {
            return mongo.addRoom(positionString, description);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public void addMessage(String positionString, String message) {
        try {
            mongo.addMessage(positionString, message);
        }catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public List<String> getMessageList(String positionString) {
        try {
            return mongo.getMessageList(positionString);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        try {
            return mongo.getSetOfExitsFromRoom(positionString);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        try {
            return mongo.getPlayerByID(playerID);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        try {
            mongo.updatePlayerRecord(record);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString, int from, int to) {
        try{
            return mongo.computeListOfPlayersAt(positionString, from, to);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        try {
            return mongo.computeCountOfActivePlayers();
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public String sessionGet(String playerID) {
        try {
            return mongo.sessionGet(playerID);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public void sessionAdd(String playerID, Player player) {
        try {
            mongo.sessionAdd(playerID, player);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public void sessionRemove(String playerID) {
        try {
            mongo.sessionRemove(playerID);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public void initialize(ServerConfiguration config) {
        try {
            mongo = new MongoStorage();
            mongo.initialize(config);
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            mongo.disconnect();
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }

    @Override
    public ServerConfiguration getConfiguration() {
        try {
            return mongo.getConfiguration();
        } catch (MongoException e){
            throw new CaveStorageException("DB_TIMEOUT_EXCEPTION", e);
        }
    }
}
