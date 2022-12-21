package de.adrianlange.mcd.model;

public interface MailserverService {

  /**
   * Returns the protocol of the mailserver service. This can be SMTP for submission or IMAP or POP3 for reception.
   *
   * @return protocol of the mailserver service
   */
  Protocol getProtocol();


  /**
   * Returns the socket type od the mailserver configuration. This can eiter be PLAIN, STARTTLS or SSL.
   *
   * @return Socket type of the service or <code>null</code> if unknown
   */
  SocketType getSocketType();


  /**
   * Host URL of the mailserver service.
   *
   * @return Host URL. May be <code>null</code> if this configuration should not be used / the protocol is not
   * supported!
   */
  String getHost();


  /**
   * Port of the mailserver service.
   *
   * @return Port. May be <code>null</code> if this configuration should not be used / the protocol is not supported!
   */
  Integer getPort();


  /**
   * Priority of the mailserver service. A service with a smaller priority should be preferred over a service with a
   * larger priority. This also applies across protocols, for example, it can be made clear that the connection via IMAP
   * should be preferred to one via POP3.
   *
   * @return Priority of the mailserver service or <code>null</code> if not specified
   */
  Integer getPriority();


  /**
   * Returns the configuration method over which the mailserver configuration has been discovered.
   *
   * @return Mailserver configuration.
   */
  ConfigurationMethod getConfigurationMethod();
}
