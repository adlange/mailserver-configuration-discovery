package de.adrianlange.mcd.strategy;

import de.adrianlange.mcd.model.MailserverService;

import java.util.List;


/**
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
   * methods, like SRV resource records or Mozilla Autoconf.
   */
  List<MailserverService> getMailserverServices( EmailAddress emailAddress );


  /**
   * Get a list of MailserverServices representing a specific mailserver protocol configuration for submission and
   * reception of emails. If the whole email address is known or should be used for the lookup, please use
   * {@link #getMailserverServices(EmailAddress)} instead.
   *
   * @param domainPart Email address domain part to get mailserver configurations for.
   * @return A list of mailserver services. The list can contain duplicate configurations published using different
   * methods, like SRV resource records or Mozilla Autoconf.
   */
  List<MailserverService> getMailserverServices( EmailAddress.DomainPart domainPart );
}
