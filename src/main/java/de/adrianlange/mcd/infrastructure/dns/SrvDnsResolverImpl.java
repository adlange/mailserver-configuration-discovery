package de.adrianlange.mcd.infrastructure.dns;

import de.adrianlange.mcd.DnsLookupContext;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.Collection;


public class SrvDnsResolverImpl extends AbstractDnsResolverImpl implements SrvDnsResolver {

  public SrvDnsResolverImpl( DnsLookupContext dnsLookupContext ) {

    super( dnsLookupContext );
  }


  public Collection<SRVRecord> getSrvRecords( String domain, String protocolPrefix ) {

    var lookupDomain = protocolPrefix + "._tcp." + domain;
    return getRecords( lookupDomain, Type.SRV ).stream().map( SRVRecord.class::cast ).toList();
  }
}
