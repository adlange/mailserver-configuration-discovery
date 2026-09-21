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
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.Collection;
import java.util.stream.Collectors;


public class SrvDnsResolverImpl extends AbstractDnsResolverImpl implements SrvDnsResolver {

  public SrvDnsResolverImpl( DnsLookupContext dnsLookupContext ) {

    super( dnsLookupContext );
  }


  /**
   * Constructor for tests, allows injecting the dnsjava resolver.
   */
  SrvDnsResolverImpl( Resolver resolver ) {

    super( resolver );
  }


  public Collection<SRVRecord> getSrvRecords( String domain, String protocolPrefix ) {

    var lookupDomain = protocolPrefix + "._tcp." + domain;
    return getRecords( lookupDomain, Type.SRV ).stream().map( SRVRecord.class::cast ).collect( Collectors.toList() );
  }
}
