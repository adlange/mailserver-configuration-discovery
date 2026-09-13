package de.adrianlange.mcd;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.Protocol;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executor;


/**
 * Main context for mailserver configuration discovery. Use builder
 * {@link MailserverConfigurationDiscoveryContextBuilder} to build it.
 */
public interface MailserverConfigurationDiscoveryContext {

  /**
   * Returns the DNS lookup context.
   *
   * @return DNS lookup context
   */
  DnsLookupContext getDnsLookupContext();


  /**
   * Returns the set of discovery scopes for what kind of mailserver configurations should be discovered.
   *
   * @return Discovery scopes
   */
  Set<DiscoveryScope> getDiscoveryScopes();


  /**
   * Returns the set of configuration methods to use for discovery.
   *
   * @return Configuration methods
   */
  Set<ConfigurationMethod> getConfigurationMethods();


  /**
   * Returns the executor used for concurrent mailserver configuration lookups.
   *
   * @return Executor
   */
  Executor getExecutor();


  /**
   * Returns whether configuration documents may additionally be fetched over plain, unencrypted HTTP. By default only
   * HTTPS is used, because a configuration document fetched over HTTP could be altered on the wire to point clients to
   * an attacker-controlled mailserver.
   *
   * @return <code>true</code> if HTTP URLs are queried in addition to HTTPS URLs, <code>false</code> otherwise
   */
  boolean isInsecureHttpAllowed();


  /**
   * Returns the timeout applied to HTTP(S) requests fetching configuration documents. It is used both as connect
   * timeout and as overall request timeout.
   *
   * @return HTTP timeout
   */
  Duration getHttpTimeout();


  enum DiscoveryScope {
    SUBMISSION, RECEPTION;


    /**
     * Returns the discovery scope a protocol belongs to.
     *
     * @param protocol Protocol, must not be null
     * @return {@link #SUBMISSION} for SMTP, {@link #RECEPTION} for IMAP and POP3
     */
    public static DiscoveryScope of( Protocol protocol ) {

      if( protocol == null )
        throw new IllegalArgumentException( "Protocol must not be null!" );

      return switch( protocol ) {
        case SMTP -> SUBMISSION;
        case IMAP, POP3 -> RECEPTION;
      };
    }
  }
}
