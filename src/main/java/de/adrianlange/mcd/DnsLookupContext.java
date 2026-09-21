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

import java.time.Duration;
import java.util.Collection;


/**
 * Configuration context for DNS lookups.
 */
public interface DnsLookupContext {

  /**
   * Returns a collection of configured DNS servers. If nothing is set the hosts default DNS server is used. If multiple
   * DNS servers are defined, all of them are used with failover (see dnsjava's <code>ExtendedResolver</code>).
   *
   * @return A collection of DNS servers, empty if none is configured and the system's resolvers are used.
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
