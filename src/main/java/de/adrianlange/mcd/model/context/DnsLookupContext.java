package de.adrianlange.mcd.model.context;

import java.time.Duration;
import java.util.Collection;


/**
 * Configuration context for DNS lookups.
 */
public interface DnsLookupContext {

  /**
   * Returns a collection of configured DNS servers. If nothing is set the hosts default DNS server is used. If multiple
   * DNS server are defined, only one of them will be used.
   *
   * @return A collection of DNS servers or <code>null</code> if none is configured.
   */
  Collection<String> getDnsServers();


  /**
   * Returns the duration until a request runs into a timeout.
   *
   * @return duration until request runs into timeout
   */
  Duration getTimeout();


  /**
   * Returns the number of automatic retries until a request fails.
   *
   * @return Number of retries
   */
  int getRetries();


  /**
   * Returns if TCP is used. If not UDP will be used.
   *
   * @return True if TCP should be used, false otherwise
   */
  boolean isTcp();
}
