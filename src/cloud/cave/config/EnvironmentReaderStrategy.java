package cloud.cave.config;

/**
 * Strategy (FRS, p. 130) for accessing environment variables in the OS.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public interface EnvironmentReaderStrategy {

  /**
   * Read the environment variable of the given name.
   * 
   * @param environmentVariable
   *          name of the environment variable whose value must be read
   * @return the value of the environment variable in the OS/shell.
   */
  String getEnv(String environmentVariable);

}
