package de.adrianlange.mcd.model.context;

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

    var ignore = InetAddress.getByName( dnsServer );
    dnsServers.add( dnsServer );
  }


  @Override
  public Collection<String> getDnsServers() {

    return dnsServers.isEmpty() ? null : Collections.unmodifiableCollection( dnsServers );
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
