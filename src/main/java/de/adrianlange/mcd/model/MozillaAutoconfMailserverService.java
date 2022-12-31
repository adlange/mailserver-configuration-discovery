package de.adrianlange.mcd.model;

import java.util.Set;


/**
 * Representation of a mailserver configuration according to Mozilla Autoconf standard. Here the mail server
 * configurations are published in XML format on web servers.<p>See <a
 * href="https://wiki.mozilla.org/Thunderbird:Autoconfiguration">here</a> for more information.<p> In almost all input
 * fields the following placeholders can occur and must be replaced by the user:
 * <ul><li>%EMAILADDRESS% (full email address of the user)</li><li>%EMAILLOCALPART% (local part of the email address)
 * </li><li>%EMAILDOMAIN%</li></ul>
 *
 * @author Adrian Lange
 */
public interface MozillaAutoconfMailserverService extends MailserverService {

  /**
   * Returns the username to use or null if not specified.<p>May contain placeholders, see
   * {@link MozillaAutoconfMailserverService} for more information.
   *
   * @return Username, placeholder for username or null
   */
  String getUsername();


  /**
   * Returns the password of the user or null if none is specified.<p>May contain placeholders, see
   * {@link MozillaAutoconfMailserverService} for more information.
   *
   * @return Password, placeholder for password or null
   */
  String getPassword();


  /**
   * Returns the authentication methods to use. In most cases this set only contains one (or zero) elements, but can
   * also contain more entries to provide a fallback if for example OAuth2 is not supported by a client.
   *
   * @return Authentication method or an empty set if none is specified.
   */
  Set<Authentication> getAuthentications();


  /**
   * Returns OAuth2 configuration to use if {@link #getAuthentications()} returns {@link Authentication#OAUTH2}.
   *
   * @return OAuth2 configurations or an empty set of none is specified.
   */
  Set<OAuth2> getOAuth2s();
}
