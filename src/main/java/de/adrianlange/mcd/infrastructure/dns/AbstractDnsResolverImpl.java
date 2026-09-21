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
package de.adrianlange.mcd.infrastructure.dns;

import de.adrianlange.mcd.DnsLookupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


public abstract class AbstractDnsResolverImpl {

  private static final Logger LOG = LoggerFactory.getLogger( AbstractDnsResolverImpl.class );

  protected final Resolver resolver;


  public AbstractDnsResolverImpl( DnsLookupContext dnsLookupContext ) {

    this( createResolver( dnsLookupContext ) );
  }


  /**
   * Constructor for tests, allows injecting the dnsjava resolver.
   */
  AbstractDnsResolverImpl( Resolver resolver ) {

    if( resolver == null )
      throw new IllegalArgumentException( "Resolver must not be null!" );
    this.resolver = resolver;
  }


  private static Resolver createResolver( DnsLookupContext dnsLookupContext ) {

    if( dnsLookupContext == null )
      throw new IllegalArgumentException( "DNS lookup context must not be null!" );

    ExtendedResolver extendedResolver;
    var dnsServers = dnsLookupContext.getDnsServers();
    if( dnsServers == null || dnsServers.isEmpty() )
      extendedResolver = new ExtendedResolver();
    else {
      try {
        extendedResolver = new ExtendedResolver( dnsServers.toArray( new String[0] ) );
      } catch( UnknownHostException uhe ) {
        // the builder validates every server via InetAddress.getByName, so this is a programming error
        throw new IllegalArgumentException( "Configured DNS servers cannot be resolved: " + dnsServers, uhe );
      }
    }
    extendedResolver.setTimeout( dnsLookupContext.getTimeout() );
    extendedResolver.setRetries( dnsLookupContext.getRetries() );
    extendedResolver.setTCP( dnsLookupContext.isTcp() );
    return extendedResolver;
  }


  protected Collection<Record> getRecords( String lookupDomain, int type ) {

    try {
      var lookup = new Lookup( lookupDomain, type );
      lookup.setResolver( resolver );
      var lookupResult = lookup.run();

      if( lookupResult == null )
        return Collections.emptyList();

      return Arrays.stream( lookupResult ).filter( r -> r.getType() == type ).collect( Collectors.toList() );
    } catch( TextParseException e ) {
      // an unparsable name cannot have records, this is an expected outcome of discovery and not an error
      LOG.debug( "Skipping DNS lookup, {} is not a valid DNS name: {}", lookupDomain, e.getMessage() );
    }
    return Collections.emptyList();
  }
}
