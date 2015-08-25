package cloud.cave.doubles;

import java.util.*;

import cloud.cave.config.EnvironmentReaderStrategy;

/**
 * Test stub (FRS, chapter 12) AND a spy (FRS, sidebar 12.1) for reading
 * environment variables and for verifying that the proper variables are read.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class StubEnvironmentReaderStrategy implements
    EnvironmentReaderStrategy {

  private class Pair {
    public String variable; public String value;
    public Pair(String var, String value) {
      variable = var; this.value = value;
    }
  }
  
  private List<Pair> valueOfNextRead;
  int index;
  
  public StubEnvironmentReaderStrategy() {
      valueOfNextRead = new ArrayList<Pair>();
      index = 0;
  }

  @Override
  public String getEnv(String environmentVariable) {
    // Verify that UnitUnderTest is trying to access the expected
    // environment variable (Spy behavior)
    Pair pair = valueOfNextRead.get(index);
    // System.out.println(" --> index "+ index + " variable "+ pair.variable+ ", value "+pair.value);
    String expected = pair.variable;
    if (! expected.equals(environmentVariable)) {
      throw new RuntimeException("StubEnvironmentReaderStrategy: Expected env variable "+expected+" but "+
          "instead variable "+environmentVariable+" was attemted to be read.");
    }
    index++;
    // And return the value of the env variable (Stub behaviour)
    return pair.value;
  }


  public void setNextExpectation(String varName, String value) {
    valueOfNextRead.add(new Pair(varName,value));
  }

}
