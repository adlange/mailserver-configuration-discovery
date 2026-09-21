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
package de.adrianlange.mcd.strategy;

import de.adrianlange.mcd.EmailAddress;
import de.adrianlange.mcd.model.MailserverService;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * A discovery strategy for mailserver service configurations.
 *
 * @author Adrian Lange
 */
public interface MailserverConfigurationDiscoveryStrategy {

  /**
   * Get a list of MailserverServices representing a specific mailserver protocol configuration for submission and
   * reception of emails. If only the domain part is known or should be used for the lookup, please use
   * {@link #getMailserverServices(EmailAddress.DomainPart)} instead.
   *
   * @param emailAddress Email address object to get mailserver configurations for.
   * @return A list of mailserver services. The list can contain duplicate configurations published using different
   * methods, like SRV resource records or Mozilla Autoconfig.
   */
  List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress emailAddress );


  /**
   * Get a list of MailserverServices representing a specific mailserver protocol configuration for submission and
   * reception of emails. If the whole email address is known or should be used for the lookup, please use
   * {@link #getMailserverServices(EmailAddress)} instead.
   *
   * @param domainPart Email address domain part to get mailserver configurations for.
   * @return A list of mailserver services. The list can contain duplicate configurations published using different
   * methods, like SRV resource records or Mozilla Autoconfig.
   */
  List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress.DomainPart domainPart );
}
