package de.adrianlange.mcd.model.context;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Builder for a {@link MailserverConfigurationDiscoveryContext}.
 */
public class MailserverConfigurationDiscoveryContextBuilder {

  private final MailserverConfigurationDiscoveryContextImpl context;


  /**
   * Create new builder with default values.
   */
  public MailserverConfigurationDiscoveryContextBuilder() {

    context = new MailserverConfigurationDiscoveryContextImpl();
  }


  /**
   * Sets the discovery scopes to declare which kind of configurations should be discovered.
   *
   * @param discoveryScopes Scopes to discover configurations for
   * @return builder
   */
  public MailserverConfigurationDiscoveryContextBuilder withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope... discoveryScopes ) {

    context.setDiscoveryScopes( discoveryScopes == null ? Collections.emptySet() :
        Arrays.stream( discoveryScopes ).collect( Collectors.toSet() ) );
    return this;
  }


  /**
   * Adds a DNS server.
   *
   * @param dnsServer DNS server address
   * @return builder
   * @throws UnknownHostException if DNS server host name is invalid (see {@link java.net.InetAddress})
   */
  public MailserverConfigurationDiscoveryContextBuilder withDnsServer( String dnsServer ) throws UnknownHostException {

    context.getDnsLookupContext().addDnsServer( dnsServer );
    return this;
  }


  /**
   * Sets the DNS lookup timeout.
   *
   * @param timeout Timeout for DNS lookups
   * @return builder
   */
  public MailserverConfigurationDiscoveryContextBuilder withDnsLookupTimeout( Duration timeout ) {

    context.getDnsLookupContext().setTimeout( timeout );
    return this;
  }


  /**
   * Sets the number of retries for DNS lookups.
   *
   * @param retries number of retries
   * @return builder
   */
  public MailserverConfigurationDiscoveryContextBuilder withDnsLookupRetries( int retries ) {

    context.getDnsLookupContext().setRetries( retries );
    return this;
  }


  /**
   * Sets if TCP should be used for DNS lookups or UDP.
   *
   * @param useTcp set true if TCP should be used for DNS lookups, UDP is used otherwise
   * @return builder
   */
  public MailserverConfigurationDiscoveryContextBuilder useTcpForDnsLookups( boolean useTcp ) {

    context.getDnsLookupContext().setTcp( useTcp );
    return this;
  }


  public MailserverConfigurationDiscoveryContext build() {

    return context;
  }
}
