package cloud.cave.service;

import cloud.cave.domain.Direction;
import cloud.cave.domain.Player;
import cloud.cave.domain.Region;
import cloud.cave.ipc.CaveStorageException;
import cloud.cave.server.StandardServerPlayer;
import cloud.cave.server.common.*;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by Soren and Laurits on 15/09/15.
 */
public class MongoExam8Storage implements CaveStorage {
    private MongoClient mongo;
    private MongoDatabase db, dbExam;
    private MongoCollection<Document> rooms, players, messages, cache, word;
    private ServerConfiguration configuration;

    @Override
    public RoomRecord getRoom(String positionString) {

        if(rooms.find(eq("pos", positionString)).first() != null) {
            Document doc = rooms.find(eq("pos", positionString)).first();
            return new RoomRecord(doc.getString("desc"));
        }

        return null;
    }

    @Override
    public boolean addRoom(String positionString, RoomRecord description) {
        Document response = word.find().first();
        List<String> badWords = (List<String>) response.get("disallow");

        for (int i = 0; i < badWords.size(); i++) {
            if (description.description.contains(badWords.get(i))) {
                return false;
            }
        }

        if(rooms.find(eq("pos", positionString)).first() == null){
            Document room = new Document("pos", positionString)
                    .append("desc", description.description);
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
        if(players.find(eq("playerID", playerID)).first() != null){
            Document response = players.find(eq("playerID", playerID)).first();
            return new PlayerRecord(new SubscriptionRecord(response.getString("playerID"), response.getString("playerName"), response.getString("groupName"), Region.valueOf(response.getString("region"))),
                    response.getString("positionAsString"), response.getString("sessionID"));
        }
        return null;

    }

    @Override
    public void updatePlayerRecord(PlayerRecord record) {
        Document player = new Document("playerID", record.getPlayerID())
                .append("playerName", record.getPlayerName())
                .append("groupName", record.getGroupName())
                .append("region", record.getRegion().toString())
                .append("positionAsString", record.getPositionAsString())
                .append("sessionID", record.getSessionId());

        if(players.find(eq("playerID", record.getPlayerID())).first() != null){
            players.updateOne(eq("playerID", record.getPlayerID()), new Document("$set", player));
        }else{
            players.insertOne(player);
        }

    }

    @Override
    public List<PlayerRecord> computeListOfPlayersAt(String positionString, int from, int to) {
        List<PlayerRecord> list = new ArrayList<>();
        PlayerRecord p;

        MongoCursor<Document> response = players.find(eq("positionAsString", positionString)).skip(from).limit(to-from).iterator();

        try{
            while(response.hasNext()) {
                Document data = response.next();
                p = new PlayerRecord(new SubscriptionRecord(data.getString("playerID"), data.getString("playerName"), data.getString("groupName"), Region.valueOf(data.getString("region"))),
                        data.getString("positionAsString"), data.getString("sessionID"));
                list.add(p);
            }

        }finally {
            response.close();
        }

        return list;
    }

    @Override
    public int computeCountOfActivePlayers() {
        return (int) players.count(ne("sessionID", null));

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

        db = mongo.getDatabase("cave");
        rooms = db.getCollection("rooms");
        players = db.getCollection("players");
        messages = db.getCollection("messages");
        cache = db.getCollection("cache");
        dbExam = mongo.getDatabase("netiquette");
        word = dbExam.getCollection("reject");

        //Creates init rooms if not there
        this.addRoom(new Point3(0, 0, 0).getPositionString(), new RoomRecord("You are standing at the end of a road before a small brick building."));
        this.addRoom(new Point3(0, 1, 0).getPositionString(), new RoomRecord("You are in open forest, with a deep valley to one side."));
        this.addRoom(new Point3(1, 0, 0).getPositionString(), new RoomRecord("You are inside a building, a well house for a large spring."));
        this.addRoom(new Point3(-1, 0, 0).getPositionString(), new RoomRecord("You have walked up a hill, still in the forest."));
        this.addRoom(new Point3(0, 0, 1).getPositionString(), new RoomRecord("You are in the top of a tall tree, at the end of a road."));

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
