package de.adrianlange.mcd.model;

public interface SrvRecordMailserverService extends MailserverService {

  /**
   * If two services have the same priority, their selection should be based on their weight. If there are two services,
   * one with weight 7 and one with weight 3, the first one should be taken in 70% of cases. This way a load balancing
   * can be configured.
   *
   * @return Weight or <code>null</code> if not configured.
   */
  Integer getWeight();
}
