package de.adrianlange.mcd.infrastructure.dns;

import de.adrianlange.mcd.DnsLookupContext;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.Collection;


public class TxtDnsResolverImpl extends AbstractDnsResolverImpl implements TxtDnsResolver {

  public TxtDnsResolverImpl( DnsLookupContext dnsLookupContext ) {

    super( dnsLookupContext );
  }


  @Override
  public Collection<TXTRecord> getTxtRecords( String domain ) {

    return getRecords( domain, Type.SRV ).stream().map( TXTRecord.class::cast ).toList();
  }
}
