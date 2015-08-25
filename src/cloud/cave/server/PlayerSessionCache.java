package cloud.cave.server;

import cloud.cave.domain.Player;

/**
 * The role of a cache for player objects, given a player id. Used by the
 * dispatcher to quickly fetch player objects without needing database access. A
 * cache is basically a (key,value) store.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface PlayerSessionCache {

  /**
   * Get the player object corresponding to the given player id.
   * 
   * @param playerID
   *          the id of the plaeyr
   * @return null if no player id is stored in the cache, otherwise the player
   *         object
   */
  Player get(String playerID);

  /**
   * Add a player instance under the given player id
   * 
   * @param playerID
   *          id to store player instance under
   * @param player
   *          the player instance to cache
   */
  void add(String playerID, Player player);

  /**
   * Remove the player instance for the given player id from the cache
   * 
   * @param playerID
   *          player id of player instance to remove
   */
  void remove(String playerID);

}
