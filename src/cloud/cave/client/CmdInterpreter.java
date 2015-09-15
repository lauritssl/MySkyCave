package cloud.cave.client;

import java.io.*;
import java.util.List;

import cloud.cave.ipc.CaveIPCException;
import cloud.cave.ipc.CaveTimeOutException;
import org.json.simple.JSONObject;

import cloud.cave.common.PlayerSessionExpiredException;
import cloud.cave.domain.*;

/**
 * The client interpreter, implementing a classic shell based read-eval-loop
 * command line tool to log a player into the cave and allow him/her to explore
 * it.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University
 * 
 */
public class CmdInterpreter {

  private Cave cave;
  private Player player;
  
  private PrintStream systemOut;
  private InputStream systemIn;

  private int lookcount = 0;

  /**
   * Construct the interpreter.
   * 
   * @param cave
   *          the cave to log into
   * @param loginName
   *          the loginName of the player
   * @param pwd
   *          the password of the player
   * @param systemOut
   *          the print stream that acts as shell output
   * @param systemIn
   *          the input stream that act as keyboard input
   */
  public CmdInterpreter(Cave cave, String loginName, String pwd, PrintStream systemOut, InputStream systemIn) {
    this.cave = cave;

    this.systemOut = systemOut;
    this.systemIn = systemIn;
    
    Login loginResult = null;

    systemOut.println("Trying to log in player with loginName: " + loginName);

    loginResult = cave.login(loginName, pwd);

    boolean success = LoginResult.isValidLogin(loginResult.getResultCode());

    if (!success) {
      systemOut.println("*** SORRY! The login failed. Reason: "
          + loginResult.getResultCode());
      System.exit(-1);
    }
    if (loginResult.getResultCode() == LoginResult.LOGIN_SUCCESS_PLAYER_ALREADY_LOGGED_IN) {
      systemOut.println("*** WARNING! User '"
          + loginResult.getPlayer().getName() + "' is ALREADY logged in! ***");
      systemOut
          .println("*** The previous session will be disconnected. ***");
    }
    player = loginResult.getPlayer();
  }

  /**
   * The classic 'read command, evaluate command, loop' of a shell. Issue your
   * command, and see the result in the shell.
   */
  public void readEvalLoop() {
    systemOut.println("== Welcome to SkyCave, player " + player.getName()
        + " ==");

    systemOut.println(cave.describeConfiguration());
    
    systemOut.println("Type 'h' for help!");

    BufferedReader bf = new BufferedReader(new InputStreamReader(systemIn));

    systemOut
        .println("Entering command loop, type \"q\" to quit, \"h\" for help.");

    // and enter the command processing loop
    String line;
    try {
      do {
        line = bf.readLine();
        if (line.length() > 0) {
          // split into into tokes on whitespace
          String[] tokens = line.split("\\s");
          try {
            // First handle the 'short hand' notation for movement
            if (tokens[0].length() == 1) {
              char primaryCommand = line.charAt(0);

              handleSingleCharCommand(primaryCommand);

            } else {
              handleMultipleCharCommand(tokens[0], tokens);
            }
            systemOut.println();
          } catch (CaveTimeOutException e){
            switch (e.getMessage()){
              case "TIME_OUT_OPEN_CIRCUIT":
                systemOut.println("*** Sorry - no weather (open circuit) ***");
                break;
              default:
                systemOut.println("*** Sorry - weather information is not available ***");
            }

          } catch (CaveIPCException e){
            systemOut.println("*** Sorry - I cannot do that as I am disconnected from the cave, please quit ***");
          }
        }
      } while (!line.equals("q"));
    } catch (PlayerSessionExpiredException exc) {
      systemOut
          .println("**** Sorry! Another session has started with the same loginID. ***");
      systemOut
          .println("**** You have been logged out.                                 ***");
      System.exit(0);
    } catch (IOException e) {
      systemOut.println("Exception caught: " + e);
    }
    systemOut.println("Leaving SkyCave - Goodbye.");
  }

  private void handleMultipleCharCommand(String command, String[] tokens) {
    lookcount = 0;
    if (command.equals("dig") && tokens.length > 2) {
      Direction direction = getDirectionFromChar(tokens[1].charAt(0));
      // Compile the room description by putting the tokens back into a single string again
      String roomDescription = "";
      roomDescription = mergeTokens(tokens, 2);
      
      boolean isValid = player.digRoom(direction, roomDescription);
      if (isValid) {
        systemOut.println("You dug a new room in direction " + direction);
      } else {
        systemOut
            .println("You cannot dig there as there is already a room in direction "
                + direction);
      }

    } else if (command.equals("who")) {
      systemOut.println("You are: " + player.getName()+ "/"+ player.getID()+ " in Region "+player.getRegion());
      systemOut.println("   in local session: " + player.getSessionID());

    } else if (command.equals("weather")) {
      String weather = player.getWeather();
      systemOut.println("The weather at: " + player.getRegion());
      systemOut.println(weather);


    } else if (command.equals("post") && tokens.length > 1) {
      String message = mergeTokens(tokens, 1);
      player.addMessage(message);
      systemOut.println("You have posted:" + message);

    } else if (command.equals("read")) {
      List<String> messageList = player.getMessageList();
      for (String m : messageList) {
        systemOut.println(m);
      }

    } else if (command.equals("sys")) {
      systemOut.println("System information:");
      systemOut.println(cave.describeConfiguration());
      systemOut.println(player.toString());
    
    } else if (command.equals("exec")) {
      if (tokens.length > 2) {
        JSONObject response = null;
        // Create the parameter array
        String[] parameters = new String[tokens.length - 2];
        for (int i = 2; i < tokens.length; i++) {
          parameters[i - 2] = tokens[i];
        }

        response = player.execute(tokens[1], parameters);
        systemOut.println("You executed command:" + tokens[1]);
        systemOut.println("  Response as JSON: " + response);
      } else {
        systemOut
            .println("Exec commands require at least one parameter. Set it to null if irrelevant");
      }
    } else {
      systemOut.println("I do not understand that long command. (Type 'h' for help)");
    }
  }

  /**
   * Merge the tokens in array 'tokens' from
   * index 'from' into a space separated string.
   * @param tokens the array of tokens
   * @param from the starting index 
   * @return a merged string with all tokens
   * separated by space
   */
  private String mergeTokens(String[] tokens, int from) {
    String mergedString="";
    for (int i=from; i < tokens.length; i++) { mergedString += " "+tokens[i]; }
    return mergedString;
  }

  private void handleSingleCharCommand(char primaryCommand) {
    switch (primaryCommand) {
    // look
    case 'l': {
      if(lookcount == 0){
        systemOut.println(player.getLongRoomDescription());
        lookcount++;
      }else{
        int from = (lookcount * 10);
        int to = (lookcount * 10) + 9;
        for ( String p : player.getPlayersHere(from, to) ) {
          systemOut.println("  ["+from+"] " + p);
          from++;
        }
        lookcount++;
      }

      break;
    }
    // The movement commands
    case 'n': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 's': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'e': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'w': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'u': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }
    case 'd': {
      tryToMove(getDirectionFromChar(primaryCommand));
      break;
    }

    // add 20 users for testing
    case 'a':{
      lookcount = 0;
      cave.login("rwar400t", "727b9c");
      cave.login("rwar401t", "ynizl2");
      cave.login("rwar402t", "f0s4p3");
      cave.login("rwar403t", "plcs74");
      cave.login("rwar404t", "v76ifd");
      cave.login("rwar405t", "jxe9ha");
      cave.login("rwar406t", "6xp9jl");
      cave.login("rwar407t", "u3mxug");
      cave.login("rwar408t", "trv9gy");
      cave.login("rwar409t", "1d5fh3");
      cave.login("rwar410t", "zsafci");
      cave.login("rwar411t", "v324q6");
      cave.login("rwar412t", "2jdfhz");
      cave.login("rwar413t", "zja3ig");
      cave.login("rwar414t", "04nj10");
      cave.login("rwar415t", "zu5qar");
      cave.login("rwar416t", "qildw2");
      cave.login("rwar417t", "61w8sh");
      cave.login("rwar418t", "exwt5w");
      cave.login("rwar419t", "n7lzqw");
      systemOut.println("20 test users added to server");
      break;
    }

    // position
    case 'p': {
      lookcount = 0;
      systemOut.println("Your position in the cave is: "
          + player.getPosition());
      break;
    }
    // Help
    case 'h': {
      lookcount = 0;
      showHelp();
      break;
    }
    // Quit
    case 'q': {
      LogoutResult logoutResult = cave.logout(player.getID());
      systemOut.println("Logged player out, result = " + logoutResult);
      break;
    }
    default: {
      lookcount = 0;
      systemOut.println("I do not understand that command. (Type 'h' for help)");
    }
    }
  }

  private Direction getDirectionFromChar(char commandChar) {
    switch (commandChar) {
    case 'n':
      return Direction.NORTH;
    case 's':
      return Direction.SOUTH;
    case 'e':
      return Direction.EAST;
    case 'w':
      return Direction.WEST;
    case 'u':
      return Direction.UP;
    case 'd':
      return Direction.DOWN;
    default:
      throw new RuntimeException("getDirectionFromChar got wrong parameter: "
          + commandChar);
    }
  }

  private void tryToMove(Direction direction) {
    lookcount = 0;
    if ( player.move(direction) ) {
      systemOut.println("You moved "+direction);
      systemOut.println(player.getShortRoomDescription());
    } else {
      systemOut.println("There is no exit going " + direction);
    }
  }

  private void showHelp() {
    systemOut.println("=== Help on the SkyCave commands. ===");
    systemOut.println("  Many commonly used commands are single-character");
    systemOut.println("Commands:");
    systemOut.println(" n,s,e,w,d,u    :  MOVE north, south, etc;");
    systemOut.println(" q              :  QUIT sky cave;");
    systemOut.println(" h              :  HELP, print this help instructions;");
    systemOut.println(" l              :  LOOK, print long description of the room you are in;");
    systemOut.println(" p              :  POSITION, print your (x,y,z) position");
    systemOut.println("Longer Commands:");
    systemOut.println(" who            :  WHO, print info on your avatar;");
    systemOut.println(" dig [d] [desc] :  DIG room in direction [d] with description [desc];");
    systemOut.println(" post [msg]     :  POST [msg] on this room's wall;");
    systemOut.println(" read           :  READ messages on this room's wall;");
    systemOut.println(" weather        :  WEATHER in the players region;");
    systemOut.println(" sys            :  SYStem and configuration information;");

    systemOut.println(" exec [cmd] [param]* :  EXEC [cmd] with 1 or more [param]s;");
    
  }

}