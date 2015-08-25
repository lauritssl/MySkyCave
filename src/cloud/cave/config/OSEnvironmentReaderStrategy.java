package cloud.cave.config;

/**
 * Implementation of the EnvironmentReaderStrategy that couples to the OS/Java
 * library method for reading a environment variable - for production execution.
 * 
 * @author Henrik Baerbak Christensen, University of Aarhus
 * 
 */
public class OSEnvironmentReaderStrategy implements EnvironmentReaderStrategy {

  @Override
  public String getEnv(String environmentVariable) {
    return System.getenv(environmentVariable);
  }

}
