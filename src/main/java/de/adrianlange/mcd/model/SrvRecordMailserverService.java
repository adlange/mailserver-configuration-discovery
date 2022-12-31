package de.adrianlange.mcd.model;

/**
 * Representation of a mail server configuration according to RFC 6186, searching for SRV records via DNS for the SMTP,
 * IMAP and POP3 e-mail protocols.
 *
 * @author Adrian Lange
 */
public interface SrvRecordMailserverService extends MailserverService {

  /**
   * Priority of the mailserver service. A service with a smaller priority should be preferred over a service with a
   * larger priority. This also applies across protocols, for example, it can be made clear that the connection via IMAP
   * should be preferred to one via POP3.
   *
   * @return Priority of the mailserver service or <code>null</code> if not specified
   */
  Integer getPriority();


  /**
   * If two services have the same priority, their selection should be based on their weight. If there are two services,
   * one with weight 7 and one with weight 3, the first one should be taken in 70% of cases. This way a load balancing
   * can be configured.
   *
   * @return Weight or <code>null</code> if not configured.
   */
  Integer getWeight();
}
