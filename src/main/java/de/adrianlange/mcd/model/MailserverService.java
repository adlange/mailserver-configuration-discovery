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
   * Hostname of the mailserver service, e.g. <code>imap.example.com</code>.
   *
   * @return Hostname. May be <code>null</code> if this configuration should not be used / the protocol is not
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
   * Returns the configuration method over which the mailserver configuration has been discovered.
   *
   * @return Mailserver configuration.
   */
  ConfigurationMethod getConfigurationMethod();
}
