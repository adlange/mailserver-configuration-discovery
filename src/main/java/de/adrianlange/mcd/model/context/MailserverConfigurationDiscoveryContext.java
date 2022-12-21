package de.adrianlange.mcd.model.context;

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


  enum DiscoveryScope {
    SUBMISSION, RECEPTION
  }
}
