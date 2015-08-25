/** 
 * The central configuration roles
 * and implementations. Notably abstract factories are
 * defined for both the client side {@link EnvironmentClientFactory}
 * and the server side {@link EnvironmentServerFactory}. Both
 * relies on reading environment variables that must be set
 * in the shell before executing the application server (daemon)
 * or the client (cmd). These are explained in the Config
 * constants.
 * @see Config
 * 
 * */
package cloud.cave.config;

