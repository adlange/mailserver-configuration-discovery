package de.adrianlange.mcd;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy;
import de.adrianlange.mcd.strategy.mozillaautoconfig.MozillaAutoconfigMailserverConfigurationDiscoveryStrategy;
import de.adrianlange.mcd.strategy.srvrecord.SrvRecordMailserverConfigurationDiscoveryStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Entry point for the lookup of mailserver configurations.
 *
 * @author Adrian Lange
 */
public class MailserverConfigurationDiscovery {

  private static final Logger LOG = LoggerFactory.getLogger( MailserverConfigurationDiscovery.class );


  private MailserverConfigurationDiscovery() {
  }


  /**
   * Discover mailserver configurations for an email address based on the given context.<p>Use
   * {@link MailserverConfigurationDiscoveryContextBuilder} to create a new context.
   *
   * @param emailAddress Email address to lookup mailserver configurations for
   * @param context      Context for the discovery of mailserver configurations
   * @return A set of mailserver configurations without duplicates, may be empty if none was found
   */
  public static Set<MailserverService> discover( EmailAddress emailAddress,
                                                 MailserverConfigurationDiscoveryContext context ) {

    if( emailAddress == null )
      throw new IllegalArgumentException( "Email address must not be null!" );
    if( context == null )
      throw new IllegalArgumentException( "Context must not be null!" );

    return discover( getStrategies( context ), s -> s.getMailserverServices( emailAddress ) );
  }


  /**
   * Discover mailserver configurations for a domain part based on the given context.<p>Use
   * {@link MailserverConfigurationDiscoveryContextBuilder} to create a new context.
   *
   * @param domain  Domain to lookup mailserver configurations for
   * @param context Context for the discovery of mailserver configurations
   * @return A set of mailserver configurations without duplicates, may be empty if none was found
   */
  public static Set<MailserverService> discover( String domain, MailserverConfigurationDiscoveryContext context ) {

    if( domain == null )
      throw new IllegalArgumentException( "Domain must not be null!" );
    if( context == null )
      throw new IllegalArgumentException( "Context must not be null!" );

    var domainPart = EmailAddress.DomainPart.of( domain );
    return discover( getStrategies( context ), s -> s.getMailserverServices( domainPart ) );
  }


  /**
   * Discover mailserver configurations for an email address based on the given context.<p>Use
   * {@link #discover(EmailAddress, MailserverConfigurationDiscoveryContext)} customize the lookup.
   *
   * @param emailAddress Email address to lookup mailserver configurations for
   * @return A set of mailserver configurations without duplicates, may be empty if none was found
   */
  public static Set<MailserverService> discover( EmailAddress emailAddress ) {

    var context = new MailserverConfigurationDiscoveryContextBuilder().build();
    return discover( emailAddress, context );
  }


  /**
   * Discover mailserver configurations for a domain part based on the default context.<p>Use
   * {@link #discover(String, MailserverConfigurationDiscoveryContext)} customize the lookup.
   *
   * @param domain Domain to lookup mailserver configurations for
   * @return A set of mailserver configurations without duplicates, may be empty if none was found
   */
  public static Set<MailserverService> discover( String domain ) {

    var context = new MailserverConfigurationDiscoveryContextBuilder().build();
    return discover( domain, context );
  }


  private static Set<MailserverConfigurationDiscoveryStrategy> getStrategies( MailserverConfigurationDiscoveryContext context ) {
    Set<MailserverConfigurationDiscoveryStrategy> strategies = new HashSet<>();

    if( context.getConfigurationMethods().contains( ConfigurationMethod.MOZILLA_AUTOCONFIG ) )
      strategies.add( new MozillaAutoconfigMailserverConfigurationDiscoveryStrategy( context ) );

    if( context.getConfigurationMethods().contains( ConfigurationMethod.RFC_6186 ) )
      strategies.add( new SrvRecordMailserverConfigurationDiscoveryStrategy( context ) );

    // TODO add autodiscover method

    return strategies;
  }


  /**
   * Runs the given strategies and merges their results into one deduplicated set.
   * <p>A failing strategy (an exception while starting it or a failed future) never fails the whole discovery: the
   * failure is logged on WARN level and the results of the other strategies and futures are returned. Package-private
   * for tests.
   *
   * @param strategies Strategies to run
   * @param lookup     Function starting the lookup on a strategy, e.g. for an email address or a domain
   * @return A set of {@link MailserverService} without duplicates, may be empty
   */
  static Set<MailserverService> discover( Collection<MailserverConfigurationDiscoveryStrategy> strategies,
                                          Function<MailserverConfigurationDiscoveryStrategy,
                                              List<CompletableFuture<List<MailserverService>>>> lookup ) {

    List<CompletableFuture<List<MailserverService>>> futures = new ArrayList<>();
    for( MailserverConfigurationDiscoveryStrategy strategy : strategies ) {
      var strategyName = strategy.getClass().getSimpleName();
      try {
        for( CompletableFuture<List<MailserverService>> future : lookup.apply( strategy ) )
          futures.add( future.handle( ( result, throwable ) -> handleResult( strategyName, result, throwable ) ) );
      } catch( RuntimeException e ) {
        LOG.warn( "Discovery strategy {} could not be started, its results are skipped", strategyName, e );
      }
    }

    //@formatter:off
    return futures.stream()
        .map( CompletableFuture::join )
        .flatMap( List::stream )
        .collect( Collectors.toSet() );
    //@formatter:on
  }


  private static List<MailserverService> handleResult( String strategyName, List<MailserverService> result,
                                                       Throwable throwable ) {

    if( throwable != null ) {
      var cause = throwable instanceof CompletionException && throwable.getCause() != null ? throwable.getCause() :
          throwable;
      LOG.warn( "Discovery strategy {} failed, its results are skipped", strategyName, cause );
      return Collections.emptyList();
    }
    return result == null ? Collections.emptyList() : result;
  }
}
