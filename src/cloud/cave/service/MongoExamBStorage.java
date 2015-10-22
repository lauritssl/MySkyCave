package cloud.cave.service;

import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.domain.Region;
import cloud.cave.ipc.CaveStorageException;
import cloud.cave.server.common.*;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

/**
 * Created by Soren and Laurits on 15/09/15.
 */
public class MongoExamBStorage implements CaveStorage {
    private MongoClient mongo;
    private MongoDatabase db;
    private MongoCollection<Document> rooms, players, messages, cache;
    private ServerConfiguration configuration;

    @Override
    public RoomRecord getRoom(String positionString) {

        if(rooms.find(eq("pos", positionString)).first() != null) {
            Document doc = rooms.find(eq("pos", positionString)).first();
            return new RoomRecord(doc.getString("room_description"));
        }

        return null;
    }
    
    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        if(rooms.find(eq("pos", positionString)).first() == null){
            Document room = new Document("pos", positionString)
                    .append("room_description", description.description);
            rooms.insertOne(room);
            return true;
        }

        return false;
    }

    @Override
    public void addMessage(String positionString, String message) {
        Document msg = new Document("pos", positionString)
                .append("msg", message);
        messages.insertOne(msg);
    }

    @Override
    public List<String> getMessageList(String positionString) {
        List<String> list = new ArrayList<>();

        MongoCursor<Document> response = messages.find(eq("pos", positionString)).iterator();

        try{
            while(response.hasNext()) {
                Document data = response.next();
                list.add(data.getString("msg"));
            }

        }finally {
            response.close();
        }

        return list;
    }

    @Override
    public List<Direction> getSetOfExitsFromRoom(String positionString) {
        List<Direction> listOfExits = new ArrayList<Direction>();
        Point3 pZero = Point3.parseString(positionString);
        Point3 p;
        for ( Direction d : Direction.values()) {
            p = new Point3(pZero.x(), pZero.y(), pZero.z());
            p.translate(d);
            String position = p.getPositionString();
            if (rooms.find(eq("pos", position)).first() != null) {
                listOfExits.add(d);
            }
        }
        return listOfExits;
    }

    @Override
    public PlayerRecord getPlayerByID(String playerID) {
        if(players.find(eq("player_id", playerID)).first() != null){
            Document response = players.find(eq("player_id", playerID)).first();
            return new PlayerRecord(new SubscriptionRecord(response.getString("player_id"), response.getString("player_name"), response.getString("group_name"), Region.valueOf(response.getString("player_region"))),
                    response.getString("pos"), response.getString("session_id"));
        }
        return null;

    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        Document player = new Document("player_id", record.getPlayerID())
                .append("player_name", record.getPlayerName())
                .append("group_name", record.getGroupName())
                .append("player_region", record.getRegion().toString())
                .append("pos", record.getPositionAsString())
                .append("session_id", record.getSessionId());

        if(players.find(eq("player_id", record.getPlayerID())).first() != null){
            players.updateOne(eq("player_id", record.getPlayerID()), new Document("$set", player));
        }else{
            players.insertOne(player);
        }

    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString, int from, int to) {
        List<PlayerRecord> list = new ArrayList<>();
        PlayerRecord p;

        MongoCursor<Document> response = players.find(eq("pos", positionString)).skip(from).limit(to-from).iterator();

        try{
            while(response.hasNext()) {
                Document data = response.next();
                p = new PlayerRecord(new SubscriptionRecord(data.getString("player_id"), data.getString("player_name"), data.getString("group_name"), Region.valueOf(data.getString("player_region"))),
                        data.getString("pos"), data.getString("session_id"));
                list.add(p);
            }

        }finally {
            response.close();
        }

        return list;
    }

    @Override
    public int computeCountOfActivePlayers() {
        return (int) players.count(ne("session_id", null));

    }

    @Override
    public String sessionGet(String playerID) {
        if(cache.find(eq("playerID", playerID)).first() != null){
            return playerID;
        }
        return null;
    }

    @Override
    public void sessionAdd(String playerID, Player player) {
        Document p = new Document("playerID", playerID);
        if(cache.find(eq("playerID", playerID)).first() != null){
            cache.updateOne(eq("playerID", playerID), new Document("$set", p));
        }else{
            cache.insertOne(p);
        }
    }

    @Override
    public void sessionRemove(String playerID) {
        if(cache.find(eq("playerID", playerID)).first() != null){
            cache.deleteOne(eq("playerID", playerID));
        }
    }

    @Override
    public int computeCountOfRooms() {
        return (int) rooms.count();
    }

    @Override
    public void initialize(ServerConfiguration config) {
        this.configuration = config;

        if(config == null){
            throw new CaveStorageException("Mongo needs configuration to run");
        }

        ServerAddress[] mongoServers = new ServerAddress[config.size()];

        for (int i = 0; i < config.size(); i++) {
            mongoServers[i] = new ServerAddress(config.get(i).getHostName(), config.get(i).getPortNumber());
        }

        mongo = new MongoClient(Arrays.asList(mongoServers));

        db = mongo.getDatabase("exam_cave");
        rooms = db.getCollection("room_collection");
        players = db.getCollection("player_collection");
        messages = db.getCollection("messages");
        cache = db.getCollection("cache");


    }

    @Override
    public void disconnect() {
        mongo.close();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }
}
