package de.adrianlange.mcd.model.context;

import java.util.EnumSet;
import java.util.Set;


public class MailserverConfigurationDiscoveryContextImpl implements MailserverConfigurationDiscoveryContext {

  private final DnsLookupContextImpl dnsLookupContext;

  private Set<DiscoveryScope> discoveryScopes;


  protected MailserverConfigurationDiscoveryContextImpl() {

    this.dnsLookupContext = new DnsLookupContextImpl();
    this.discoveryScopes = EnumSet.allOf( DiscoveryScope.class );
  }


  @Override
  public DnsLookupContextImpl getDnsLookupContext() {

    return dnsLookupContext;
  }


  @Override
  public Set<DiscoveryScope> getDiscoveryScopes() {

    return discoveryScopes;
  }


  public void setDiscoveryScopes( Set<DiscoveryScope> discoveryScopes ) {

    this.discoveryScopes = discoveryScopes;
  }
}
