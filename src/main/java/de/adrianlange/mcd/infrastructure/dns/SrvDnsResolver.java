package de.adrianlange.mcd.infrastructure.dns;

import de.adrianlange.mcd.model.context.DnsLookupContext;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class SrvDnsResolver {

  private final ExtendedResolver resolver;


  public SrvDnsResolver( DnsLookupContext dnsLookupContext ) {

    if( dnsLookupContext == null )
      throw new AssertionError( "Context must not be null!" );

    if( dnsLookupContext.getDnsServers() == null )
      resolver = new ExtendedResolver();
    else {
      var servers = dnsLookupContext.getDnsServers().toArray( new String[0] );
      try {
        resolver = new ExtendedResolver( servers );
      } catch( UnknownHostException ignore ) {
        // is handled when defining the context
        throw new RuntimeException( ignore );
      }
    }
    resolver.setTimeout( dnsLookupContext.getTimeout() );
    resolver.setRetries( dnsLookupContext.getRetries() );
    resolver.setTCP( dnsLookupContext.isTcp() );
  }


  public Collection<SRVRecord> getSrvRecords( String domain, String protocolPrefix ) {

    var lookupDomain = protocolPrefix + "._tcp." + domain;
    try {
      var lookup = new Lookup( lookupDomain, Type.SRV );
      lookup.setResolver( resolver );
      var lookupResult = lookup.run();

      if( lookupResult == null )
        return Collections.emptyList();

      return Arrays.stream( lookupResult ).filter( r -> r.getType() == Type.SRV ).map( SRVRecord.class::cast ).toList();
    } catch( TextParseException e ) {
      throw new RuntimeException( e ); // TODO
    }
  }
}
