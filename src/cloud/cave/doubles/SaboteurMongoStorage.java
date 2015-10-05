package cloud.cave.doubles;

import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.ipc.CaveStorageException;
import cloud.cave.server.common.PlayerRecord;
import cloud.cave.server.common.RoomRecord;
import cloud.cave.server.common.ServerConfiguration;
import cloud.cave.service.CaveStorage;
import com.mongodb.MongoException;

import java.util.List;

/**
 * Created by lundtoft on 30/09/15.
 */
public class SaboteurMongoStorage implements CaveStorage {

    CaveStorage storrage;
    boolean failNext = false;

    @Override
    public RoomRecord getRoom(String positionString) {
        if(failNext){
            failNext = false;
            throw new MongoException("");
        }else{
            return storrage.getRoom(positionString);
        }
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.addRoom(positionString, description);
        }
    }

    @Override
    public void addMessage(String positionString, String message) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            storrage.addMessage(positionString, message);
        }
    }

    @Override
    public List<String> getMessageList(String positionString) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.getMessageList(positionString);
        }
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.getSetOfExitsFromRoom(positionString);
        }
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.getPlayerByID(playerID);
        }
    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            storrage.updatePlayerRecord(record);
        }
    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString, int from, int to) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.computeListOfPlayersAt(positionString, from, to);
        }
    }

    @Override
    public int computeCountOfActivePlayers() {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.computeCountOfActivePlayers();
        }
    }

    @Override
    public String sessionGet(String playerID) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.sessionGet(playerID);
        }
    }

    @Override
    public void sessionAdd(String playerID, Player player) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            storrage.sessionAdd(playerID, player);
        }
    }

    @Override
    public void sessionRemove(String playerID) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            storrage.sessionRemove(playerID);
        }
    }

    @Override
    public void initialize(ServerConfiguration config) {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            storrage = new FakeCaveStorage();
            storrage.initialize(config);
        }

    }

    @Override
    public void disconnect() {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            storrage.disconnect();
        }
    }

    @Override
    public ServerConfiguration getConfiguration() {
        if(failNext){
            failNext = false;
            throw new MongoException("DB_TIMEOUT_EXCEPTION");
        }else{
            return storrage.getConfiguration();
        }
    }

    public void setFailNext(boolean failNext) {
        this.failNext = failNext;
    }
}
