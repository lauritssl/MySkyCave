package cloud.cave.service;

import static org.junit.Assert.*;

import cloud.cave.config.CaveServerFactory;
import cloud.cave.domain.Login;
import cloud.cave.domain.Player;
import cloud.cave.domain.Region;
import cloud.cave.ipc.CaveStorageException;
import cloud.cave.server.StandardServerCave;
import cloud.cave.server.StandardServerPlayer;
import org.junit.Before;
import org.junit.Test;

import cloud.cave.doubles.*;
import cloud.cave.server.common.*;

/**
 * Created by lundtoft on 30/09/15.
 */
public class TestAvailabilityFailover {
    private FailsafeMongoStorage storage;
    private SaboteurMongoStorage saboteurStorrage;

    @Before
    public void setUp() throws Exception {
        storage = new FailsafeMongoStorage();
        saboteurStorrage = new SaboteurMongoStorage();
        saboteurStorrage.initialize(null);
        storage.setStorage(saboteurStorrage); //Changing the storage to saboteur
    }

    @Test
    public void shouldThrowExceptionOnGetRoom(){
        String pos = new Point3(0,0,0).toString();
        assertNotNull(storage.getRoom(pos));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.getRoom(pos);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnAddRoom(){
        String pos = new Point3(0,0,0).toString();
        RoomRecord description = new RoomRecord("Test");
        assertNotNull(storage.addRoom(pos, description));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            pos = new Point3(0,0,1).toString();
            description = new RoomRecord("Test2");
            storage.addRoom(pos, description);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnAddMessage(){
        String pos = new Point3(0,0,0).toString();
        storage.addMessage(pos, "Test");

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.addMessage(pos, "Test");
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnGetMessageList(){
        String pos = new Point3(0,0,0).toString();
        assertNotNull(storage.getMessageList(pos));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.getMessageList(pos);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnGetSetOfExitsFromRoom(){
        String pos = new Point3(0,0,0).toString();
        assertNotNull(storage.getSetOfExitsFromRoom(pos));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.getSetOfExitsFromRoom(pos);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnGetPlayerByID(){
        //Make sure that we return null (if mongo fails an exception is thrown and null is not returned)
        assertNull(storage.getPlayerByID("none"));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.getPlayerByID("none");
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnUpdatePlayerRecord(){
        SubscriptionRecord sr = new SubscriptionRecord("", "", "", Region.AARHUS);
        String pos = new Point3(0,0,0).toString();
        PlayerRecord pr = new PlayerRecord(sr, pos, "");

        storage.updatePlayerRecord(pr);

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.updatePlayerRecord(pr);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnComputeListOfPlayersAt(){
        String pos = new Point3(0,0,0).toString();
        assertNotNull(storage.computeListOfPlayersAt(pos, 0, 9));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.computeListOfPlayersAt(pos, 0, 9);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnComputeCountOfActivePlayers(){
        String pos = new Point3(0,0,0).toString();
        assertEquals("There should be no active players", 0, storage.computeCountOfActivePlayers());

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.computeCountOfActivePlayers();
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnSessionAdd(){

        //Needs to get player on a cave
        CaveServerFactory factory = new AllTestDoubleFactory();
        StandardServerCave cave = new StandardServerCave(factory);

        Login loginResult = cave.login( "magnus_aarskort", "312");
        Player p1 = loginResult.getPlayer();

        storage.sessionAdd("", p1);

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try{
            storage.sessionAdd("", null);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnSessionGet(){
        //Should return null as there is no player with blank session id
        assertNull(storage.sessionGet(""));

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try {
            storage.sessionGet("");
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnSessionRemove(){

        storage.sessionRemove("");

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try {
            storage.sessionRemove("");
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnDisconnect(){

        storage.disconnect();

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try {
            storage.disconnect();
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnGetConfiguration(){

        //Initialized with null, this should return null
        assertNull(storage.getConfiguration());

        saboteurStorrage.setFailNext(true);

        Exception exception = new Exception();
        try {
            storage.getConfiguration();
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
    }

    @Test
    public void shouldThrowExceptionOnInitialize(){

        //Should get an exception when initialized with null (as mongo needs real configuration)
        Exception exception = new Exception();
        try {
            storage.initialize(null);
        }catch (Exception e){
            exception = e;
        }

        assertEquals("Should have thrown a CaveStorageException", CaveStorageException.class, exception.getClass());
        
    }
}
