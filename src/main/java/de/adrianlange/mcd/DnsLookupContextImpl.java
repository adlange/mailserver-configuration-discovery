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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class DnsLookupContextImpl implements DnsLookupContext {

  private final Set<String> dnsServers = new HashSet<>();

  private Duration timeout = Duration.ofSeconds( 10 );

  private int retries = 3;

  private boolean tcp = false;


  protected DnsLookupContextImpl() {

  }


  public void addDnsServer( String dnsServer ) throws UnknownHostException {

    InetAddress.getByName( dnsServer );
    dnsServers.add( dnsServer );
  }


  @Override
  public Collection<String> getDnsServers() {

    return Collections.unmodifiableCollection( dnsServers );
  }


  @Override
  public Duration getTimeout() {

    return timeout;
  }


  public void setTimeout( Duration timeout ) {

    this.timeout = timeout;
  }


  @Override
  public int getRetries() {

    return retries;
  }


  public void setRetries( int retries ) {

    this.retries = retries;
  }


  @Override
  public boolean isTcp() {

    return tcp;
  }


  public void setTcp( boolean tcp ) {

    this.tcp = tcp;
  }
}
