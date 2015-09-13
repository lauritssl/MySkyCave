package cloud.cave.doubles;

import java.util.*;

import org.mindrot.jbcrypt.BCrypt;

import cloud.cave.domain.*;
import cloud.cave.server.common.*;
import cloud.cave.service.*;

/**
 * A test stub implementation of the subscription storage. It initially knows
 * only three loginNames, and their associated passwords and playerNames.
 * <p>
 * Note that the hardcoded playerIDs (user-001 .. user-003) are also
 * hardcoded in the stub weather service, thus changing these will 
 * require rewriting a lot of test code and stub code.
 * <p>
 * Note that the implementation here does NOT store passwords but uses jBCrypt
 * to store password hashes, which is a standard technique to guard
 * passwords safely even if a database's contents is stolen
 *  
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class TestStubSubscriptionService implements SubscriptionService {
  
  public TestStubSubscriptionService() {
    super();
    subscriptionMap = new HashMap<String,SubscriptionPair>();
    // populate with the three users known by all test cases
    subscriptionMap.put("mikkel_aarskort", 
        new SubscriptionPair("123",
        new SubscriptionRecord("user-001","Mikkel", "grp01", Region.AARHUS)));
    subscriptionMap.put("magnus_aarskort", 
        new SubscriptionPair("312",
        new SubscriptionRecord("user-002","Magnus", "grp01", Region.COPENHAGEN)));
    subscriptionMap.put("mathilde_aarskort", 
        new SubscriptionPair("321",
        new SubscriptionRecord("user-003","Mathilde", "grp02", Region.AALBORG)));

    //20 extra players added for testing
    subscriptionMap.put("rwar400t", new SubscriptionPair("727b9c", new SubscriptionRecord("user-004", "user-004", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar401t", new SubscriptionPair("ynizl2", new SubscriptionRecord("user-005", "user-005", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar402t", new SubscriptionPair("f0s4p3", new SubscriptionRecord("user-006", "user-006", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar403t", new SubscriptionPair("plcs74", new SubscriptionRecord("user-007", "user-007", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar404t", new SubscriptionPair("v76ifd", new SubscriptionRecord("user-008", "user-008", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar405t", new SubscriptionPair("jxe9ha", new SubscriptionRecord("user-009", "user-009", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar406t", new SubscriptionPair("6xp9jl", new SubscriptionRecord("user-010", "user-010", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar407t", new SubscriptionPair("u3mxug", new SubscriptionRecord("user-011", "user-011", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar408t", new SubscriptionPair("trv9gy", new SubscriptionRecord("user-012", "user-012", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar409t", new SubscriptionPair("1d5fh3", new SubscriptionRecord("user-013", "user-013", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar410t", new SubscriptionPair("zsafci", new SubscriptionRecord("user-014", "user-014", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar411t", new SubscriptionPair("v324q6", new SubscriptionRecord("user-015", "user-015", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar412t", new SubscriptionPair("2jdfhz", new SubscriptionRecord("user-016", "user-016", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar413t", new SubscriptionPair("zja3ig", new SubscriptionRecord("user-017", "user-017", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar414t", new SubscriptionPair("04nj10", new SubscriptionRecord("user-018", "user-018", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar415t", new SubscriptionPair("zu5qar", new SubscriptionRecord("user-019", "user-019", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar416t", new SubscriptionPair("qildw2", new SubscriptionRecord("user-020", "user-020", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar417t", new SubscriptionPair("61w8sh", new SubscriptionRecord("user-021", "user-021", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar418t", new SubscriptionPair("exwt5w", new SubscriptionRecord("user-022", "user-022", "RWA4", Region.AARHUS)));
    subscriptionMap.put("rwar419t", new SubscriptionPair("n7lzqw", new SubscriptionRecord("user-023", "user-023", "RWA4", Region.AARHUS)));

    // and populate with a single 'reserved' user which is used by the
    // course's automatic testing system. Leave this reserved login
    // in the test stub because otherwise our grading system will not
    // pass its tests and then you will not get the proper points for
    // your score. The reserved user is not used by any of the test
    // cases.
    subscriptionMap.put("reserved_aarskort", 
        new SubscriptionPair("cloudarch",
        new SubscriptionRecord("user-reserved","ReservedCrunchUser", "zzz0", Region.AARHUS)));
    
  }

  private class SubscriptionPair {
    public SubscriptionPair(String password, SubscriptionRecord record) {
      String salt = BCrypt.gensalt(4); // Preferring faster over security
      String hash = BCrypt.hashpw(password, salt);
      
      this.bCryptHash = hash;
      this.subscriptionRecord = record;
    }
    public String bCryptHash;
    public SubscriptionRecord subscriptionRecord;
  }
  
  /** A database 'table' that has loginName as primary key (key)
   * and the subscription record as value.
   */
  private Map<String, SubscriptionPair> subscriptionMap;
  private ServerConfiguration configuration;

  @Override
  public SubscriptionRecord lookup(String loginName, String password) {
    SubscriptionPair pair = subscriptionMap.get(loginName);

    // Verify that loginName+pwd match a valid subscription
    if (pair == null || 
        ! BCrypt.checkpw(password, pair.bCryptHash)) { 
      return new SubscriptionRecord( SubscriptionResult.LOGIN_NAME_OR_PASSWORD_IS_UNKNOWN ); 
    }
    
    return pair.subscriptionRecord;
  }
  
  public String toString() {
    return "TestStubSubscriptionService (Only knows three fixed testing users)";
  }

  @Override
  public ServerConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void initialize(ServerConfiguration config) {
    this.configuration = config;
  }

  @Override
  public void disconnect() {
    // No op
  }
}