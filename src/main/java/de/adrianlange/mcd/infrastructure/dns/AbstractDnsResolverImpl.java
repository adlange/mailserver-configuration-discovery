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
      throw new AssertionError( "Context must not be null!" );

    ExtendedResolver extendedResolver;
    if( dnsLookupContext.getDnsServers() == null )
      extendedResolver = new ExtendedResolver();
    else {
      var servers = dnsLookupContext.getDnsServers().toArray( new String[0] );
      try {
        extendedResolver = new ExtendedResolver( servers );
      } catch( UnknownHostException uhe ) {
        LOG.error( "Given DNS servers may not exist: {}", dnsLookupContext.getDnsServers(), uhe );
        // is handled when defining the context
        throw new RuntimeException( uhe );
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
      LOG.error( "Could not lookup domain {}", lookupDomain, e );
    }
    return Collections.emptyList();
  }
}
