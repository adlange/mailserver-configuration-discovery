/*
 * Copyright 2022-2026 Adrian Lange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adrianlange.mcd;

import de.adrianlange.mcd.model.ConfigurationMethod;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MailserverConfigurationDiscoveryContextImpl implements MailserverConfigurationDiscoveryContext {

  private final DnsLookupContextImpl dnsLookupContext;

  private Set<DiscoveryScope> discoveryScopes;

  private Set<ConfigurationMethod> configurationMethods;

  private Executor executor;

  private boolean insecureHttpAllowed;

  private Duration httpTimeout;


  protected MailserverConfigurationDiscoveryContextImpl() {

    this.dnsLookupContext = new DnsLookupContextImpl();
    this.discoveryScopes = EnumSet.allOf( DiscoveryScope.class );
    this.configurationMethods = EnumSet.allOf( ConfigurationMethod.class );
    // discovery is blocking DNS and HTTP I/O, virtual threads fit that best and need no pool sizing or shutdown
    this.executor = Executors.newVirtualThreadPerTaskExecutor();
    this.insecureHttpAllowed = false;
    this.httpTimeout = Duration.ofSeconds( 10 );
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


  @Override
  public boolean isInsecureHttpAllowed() {
    return insecureHttpAllowed;
  }


  public void setInsecureHttpAllowed( boolean insecureHttpAllowed ) {
    this.insecureHttpAllowed = insecureHttpAllowed;
  }


  @Override
  public Duration getHttpTimeout() {
    return httpTimeout;
  }


  public void setHttpTimeout( Duration httpTimeout ) {
    this.httpTimeout = httpTimeout;
  }
}
