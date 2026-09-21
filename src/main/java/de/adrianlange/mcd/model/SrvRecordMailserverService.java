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

/**
 * Representation of a mail server configuration according to RFC 6186, searching for SRV records via DNS for the SMTP,
 * IMAP and POP3 e-mail protocols. The <code>_submissions</code> label from RFC 8314 is supported as well.
 * <p>The socket type is only known for the implicit-TLS labels <code>_submissions</code>, <code>_imaps</code> and
 * <code>_pop3s</code> ({@link SocketType#SSL}). For <code>_submission</code>, <code>_imap</code> and
 * <code>_pop3</code> the SRV record does not tell whether STARTTLS is offered, so {@link #getSocketType()} returns
 * <code>null</code>.
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
