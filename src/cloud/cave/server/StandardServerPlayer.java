package cloud.cave.server;

import java.util.*;

import org.json.simple.JSONObject;

import cloud.cave.domain.*;
import cloud.cave.ipc.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * Standard implementation of Player on the server side. Interacts with
 * underlying storage for mutator methods, and some of the more complex accessor
 * methods.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class StandardServerPlayer implements Player {

  /** The classpath used to search for Command objects
   */
  public static final String EXTENSION_CLASSPATH = "cloud.cave.extension";

  private CaveStorage storage;
  private String ID;
  private String sessionId;

  // These attributes of the player are essentially
  // caching of the 'true' information which is stored in
  // the underlying cave storage.
  private String name;
  private String groupName;
  private Region region;
  private RoomRecord currentRoom;
  private String position;

  private WeatherService weatherService;

  /**
   * Never call this constructor directly, use cave.login() instead!
   * Create a player instance bound to the given delegates for
   * session caching, persistence, etc.
   * 
   * @param playerID
   *          the player's id
   * @param storage
   *          the storage service connector for the cave and players
   * @param weatherService
   *          the weather service connector
   * @param sessionCache the cache of player sessions to use
   */
  StandardServerPlayer(String playerID, CaveStorage storage,
      WeatherService weatherService, PlayerSessionCache sessionCache) {
    super();
    this.ID = playerID;
    this.storage = storage;
    this.weatherService = weatherService;
    
    refreshFromStorage();
  }

  @Override
  public String getName() {
    return name;
  }

  public String getGroupName() {
    return groupName;
  }

  @Override
  public String getID() {
    return ID;
  }

  @Override
  public String getShortRoomDescription() {
    return currentRoom.description;
  }

  @Override
  public String getLongRoomDescription() {
    String allOfIt = getShortRoomDescription()+"\nThere are exits in directions:\n";
    for ( Direction dir : getExitSet()) {
      allOfIt += "  "+dir+" ";
    }
    allOfIt += "\nYou see other players:\n";
    List<String> playerNameList = getPlayersHere();
    int count = 0;
    for ( String p : playerNameList ) {
      allOfIt += "  ["+count+"] " + p;
      count ++;
    }
    return allOfIt;
  }

  @Override
  public List<Direction> getExitSet() {
    return storage.getSetOfExitsFromRoom(position);
  }

  @Override
  public Region getRegion() {
    return region;
  }

  @Override
  public String getPosition() {
    return storage.getPlayerByID(getID()).getPositionAsString();
  }

  @Override
  public List<String> getPlayersHere() {
    List<PlayerRecord> playerList = storage.computeListOfPlayersAt(getPosition());
    List<String> playerNameList = new ArrayList<String>();
    for (PlayerRecord record: playerList) {
      playerNameList.add(record.getPlayerName());
    }
    return playerNameList;
  }
  
  @Override
  public String getSessionID() {
    return sessionId;
  }

  @Override
  public void addMessage(String message) {
    // TODO Empty stub, to be implemented by students
  }

  @Override
  public List<String> getMessageList() {
    // TODO Empty stub, to be implemented by students
    List<String> contents = new ArrayList<String>();
    contents.add("NOT IMPLEMENTED YET");
    return contents;
  }

  @Override
  public String getWeather() {
    JSONObject weatherAsJson = 
        weatherService.requestWeather(getGroupName(), getID(), getRegion());
    String weather = convertToFormattedString(weatherAsJson);
    return weather;
  }
  
  /**
   * Convert a JSON object that represents weather in the format of the cave
   * weather service into the string representation defined for the cave UI.
   * 
   * @param currentObservation
   *          weather information formatted as JSON
   * @return formatted string describing the weather
   */
  private String convertToFormattedString(JSONObject currentObservation) {

    String result = null;
    if (currentObservation.get("authenticated").equals("true")) {
      String temperature = currentObservation.get("temperature").toString();
      double tempDouble = Double.parseDouble(temperature);

      String feelslike = currentObservation.get("feelslike").toString();
      double feelDouble = Double.parseDouble(feelslike);

      String winddir = currentObservation.get("winddirection").toString();

      String windspeed = currentObservation.get("windspeed").toString();
      double windSpDouble = Double.parseDouble(windspeed);

      String weather = currentObservation.get("weather").toString();

      String time = currentObservation.get("time").toString();

      result = String
          .format(
              Locale.US,
              "The weather in %s is %s, temperature %.1fC (feelslike %.1fC). Wind: %.1f m/s, direction %s. This report is dated: %s.",
              getRegion().toString(), weather, tempDouble, feelDouble,
              windSpDouble, winddir, time);
    } else {
      result = "The weather service failed with message: "
          + currentObservation.get("errorMessage");
    }
    return result;
  }

  @Override
  public boolean move(Direction direction) {
    // Calculate the offsets in the given direction
    Point3 p = Point3.parseString(position);
    p.translate(direction);
    // convert to the new position in string format
    String newPosition = p.getPositionString();
    // get the room in that direction
    RoomRecord newRoom = storage.getRoom(newPosition);
    // if it is null, then there is no room in that direction
    if ( newRoom == null ) { return false; }
    // otherwise update internal state
    position = p.getPositionString();
    currentRoom = newRoom;
    
    // and update this player's position in the storage
    PlayerRecord pRecord = storage.getPlayerByID(this.getID());
    pRecord.setPositionAsString(position);
    storage.updatePlayerRecord(pRecord);
    return true;
  }

  @Override
  public boolean digRoom(Direction direction, String description) {
    // Calculate the offsets in the given direction
    Point3 p = Point3.parseString(position);
    p.translate( direction);
    RoomRecord room = new RoomRecord(description);
    return storage.addRoom(p.getPositionString(), room);
  }

  @Override
  public JSONObject execute(String commandName, String... parameters) {
    // Compute the qualified path of the command class that shall be loaded
    String qualifiedClassName = EXTENSION_CLASSPATH + "." + commandName;

    // Load it
    Class<?> theClass = null;
    try {
      theClass = Class.forName(qualifiedClassName);
    } catch (ClassNotFoundException e) {
      return Marshaling.createInvalidReplyWithExplantion(StatusCode.SERVER_FAILED_TO_LOAD_COMMAND,
          "Player.execute failed to load Command class: "+commandName);
    }
    
    // Next, instantiate the command object
    Command command = null; 
    try {
      command = (Command) theClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      return Marshaling.createInvalidReplyWithExplantion(StatusCode.SERVER_FAILED_TO_INSTANTIATE_COMMAND,
          "Player.execute failed to instantiate Command object: "+commandName);
    }
    
    // Initialize the command object
    command.setPlayerID(getID());
    command.setStorageService(storage);
    
    // And execute the command...
    JSONObject reply = command.execute(parameters);
    
    // as the command may update any aspect of the player' data
    // and as we cache it here locally, invalidate the caching
    refreshFromStorage();
    
    return reply;
  }

  private void refreshFromStorage() {
    PlayerRecord pr = storage.getPlayerByID(ID);
    name = pr.getPlayerName();
    groupName = pr.getGroupName();
    position = pr.getPositionAsString();
    region = pr.getRegion();
    sessionId = pr.getSessionId(); 

    currentRoom = storage.getRoom(position);
  }

  @Override
  public String toString() {
    return "StandardServerPlayer [storage=" + storage + ", name=" + name
        + ", ID=" + ID + ", region=" + region + ", currentRoom=" + currentRoom
        + ", position=" + position + ", sessionId=" + sessionId + "]";
  }
}
