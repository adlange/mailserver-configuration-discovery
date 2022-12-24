package de.adrianlange.mcd;

import de.adrianlange.mcd.model.ConfigurationMethod;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;


public class MailserverConfigurationDiscoveryContextImpl implements MailserverConfigurationDiscoveryContext {

  private final DnsLookupContextImpl dnsLookupContext;

  private Set<DiscoveryScope> discoveryScopes;

  private Set<ConfigurationMethod> configurationMethods;

  private Executor executor;


  protected MailserverConfigurationDiscoveryContextImpl() {

    this.dnsLookupContext = new DnsLookupContextImpl();
    this.discoveryScopes = EnumSet.allOf( DiscoveryScope.class );
    this.configurationMethods = EnumSet.allOf( ConfigurationMethod.class );
    this.executor = new ForkJoinPool();
  }


  @Override
  public DnsLookupContextImpl getDnsLookupContext() {

    return dnsLookupContext;
  }


  @Override
  public Set<DiscoveryScope> getDiscoveryScopes() {

    return Collections.unmodifiableSet( discoveryScopes );
  }


  public void setDiscoveryScopes( Set<DiscoveryScope> discoveryScopes ) {

    this.discoveryScopes = discoveryScopes;
  }


  @Override
  public Set<ConfigurationMethod> getConfigurationMethods() {
    return Collections.unmodifiableSet( configurationMethods );
  }


  public void setConfigurationMethods( Set<ConfigurationMethod> configurationMethods ) {
    this.configurationMethods = configurationMethods;
  }


  @Override
  public Executor getExecutor() {
    return executor;
  }


  public void setExecutor( Executor executor ) {
    this.executor = executor;
  }
}
