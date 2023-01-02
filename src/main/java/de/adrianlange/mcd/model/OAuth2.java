package de.adrianlange.mcd.model;

/**
 * Representation of a OAuth2 configuration.<p>May contain placeholders, see {@link MozillaAutoconfMailserverService}
 * for more information.
 *
 * @author Adrian Lange
 */
public interface OAuth2 {

  /**
   * Returns the token issuer.<p>May contain placeholders, see {@link MozillaAutoconfMailserverService} for more
   * information.
   *
   * @return Name of the issuer or null if not set.
   */
  String getIssuer();


  /**
   * Returns the token scope.<p>May contain placeholders, see {@link MozillaAutoconfMailserverService} for more
   * information.
   *
   * @return Name of the scope or null if not set.
   */
  String getScope();


  /**
   * Returns the auth URL.<p>May contain placeholders, see {@link MozillaAutoconfMailserverService} for more
   * information.
   *
   * @return Auth URL or null if not set.
   */
  String getAuthUrl();


  /**
   * Returns the token URL.<p>May contain placeholders, see {@link MozillaAutoconfMailserverService} for more
   * information.
   *
   * @return Token URL or null if not set.
   */
  String getTokenUrl();
}
