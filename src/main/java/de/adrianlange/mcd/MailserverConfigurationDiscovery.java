package de.adrianlange.mcd;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.strategy.EmailAddress;
import de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy;
import de.adrianlange.mcd.strategy.srvrecord.SrvRecordMailserverConfigurationDiscoveryStrategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Entry point for the lookup of mailserver configurations.
 *
 * @author Adrian Lange
 */
public class MailserverConfigurationDiscovery {

  private MailserverConfigurationDiscovery() {
  }


  /**
   * Discover mailserver configurations for an email address based on the given context.<p>Use
   * {@link MailserverConfigurationDiscoveryContextBuilder} to create a new context.
   *
   * @param emailAddress Email address to lookup mailserver configurations for
   * @param context      Context for the discovery of mailserver configurations
   * @return A list of mailserver configurations, may be empty if none was found
   */
  public static List<MailserverService> discover( EmailAddress emailAddress,
                                                  MailserverConfigurationDiscoveryContext context ) {

    if( emailAddress == null )
      throw new IllegalArgumentException( "Email address must not be null!" );
    if( context == null )
      throw new IllegalArgumentException( "Context must not be null!" );

    return getStrategies( context ).stream().map( s -> s.getMailserverServices( emailAddress ) ).flatMap( List::stream ).toList();
  }


  /**
   * Discover mailserver configurations for a domain part based on the given context.<p>Use
   * {@link MailserverConfigurationDiscoveryContextBuilder} to create a new context.
   *
   * @param domain  Domain to lookup mailserver configurations for
   * @param context Context for the discovery of mailserver configurations
   * @return A list of mailserver configurations, may be empty if none was found
   */
  public static List<MailserverService> discover( String domain, MailserverConfigurationDiscoveryContext context ) {

    if( domain == null )
      throw new IllegalArgumentException( "Domain must not be null!" );
    if( context == null )
      throw new IllegalArgumentException( "Context must not be null!" );

    return getStrategies( context ).stream().map( s -> s.getMailserverServices( EmailAddress.DomainPart.of( domain ) ) ).flatMap( List::stream ).toList();
  }


  /**
   * Discover mailserver configurations for an email address based on the given context.<p>Use
   * {@link #discover(EmailAddress, MailserverConfigurationDiscoveryContext)} customize the lookup.
   *
   * @param emailAddress Email address to lookup mailserver configurations for
   * @return A list of mailserver configurations, may be empty if none was found
   */
  public static List<MailserverService> discover( EmailAddress emailAddress ) {

    if( emailAddress == null )
      throw new IllegalArgumentException( "Email address must not be null!" );

    var context = new MailserverConfigurationDiscoveryContextBuilder().build();
    return getStrategies( context ).stream().map( s -> s.getMailserverServices( emailAddress ) ).flatMap( List::stream ).toList();
  }


  /**
   * Discover mailserver configurations for a domain part based on the default context.<p>Use
   * {@link #discover(String, MailserverConfigurationDiscoveryContext)} customize the lookup.
   *
   * @param domain Domain to lookup mailserver configurations for
   * @return A list of mailserver configurations, may be empty if none was found
   */
  public static List<MailserverService> discover( String domain ) {

    if( domain == null )
      throw new IllegalArgumentException( "Domain must not be null!" );

    var context = new MailserverConfigurationDiscoveryContextBuilder().build();
    return getStrategies( context ).stream().map( s -> s.getMailserverServices( EmailAddress.DomainPart.of( domain ) ) ).flatMap( List::stream ).toList();
  }


  private static Set<MailserverConfigurationDiscoveryStrategy> getStrategies( MailserverConfigurationDiscoveryContext context ) {
    Set<MailserverConfigurationDiscoveryStrategy> strategies = new HashSet<>();

    if( context.getConfigurationMethods().contains( ConfigurationMethod.RFC_61186 ) )
      strategies.add( new SrvRecordMailserverConfigurationDiscoveryStrategy( context ) );

    // TODO add strategies for other configuration methods

    return strategies;
  }
}
