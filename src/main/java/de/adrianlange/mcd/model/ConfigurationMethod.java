package de.adrianlange.mcd.model;

/**
 * Methods a mailserver configuration can be discovered with.
 */
public enum ConfigurationMethod {

  /** DNS SRV records according to RFC 6186 (and RFC 8314 for <code>_submissions</code>). */
  RFC_6186,

  /** Mozilla Autoconfig XML documents published by the domain owner. */
  MOZILLA_AUTOCONFIG
}
