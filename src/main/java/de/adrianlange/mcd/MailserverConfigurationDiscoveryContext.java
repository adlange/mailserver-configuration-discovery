package de.adrianlange.mcd;

import de.adrianlange.mcd.model.ConfigurationMethod;

import java.util.Set;


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


  enum DiscoveryScope {
    SUBMISSION, RECEPTION
  }
}
