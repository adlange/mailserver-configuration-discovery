package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MozillaAutoconfMailserverService;
import de.adrianlange.mcd.model.OAuth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class MozillaAutoconfMailserverServiceImpl extends AbstractMailserverService implements MozillaAutoconfMailserverService {

  private String username = null;

  private String password = null;

  private final Set<Authentication> authentications = new HashSet<>();

  private final Set<OAuth2> oAuth2s = new HashSet<>();


  public MozillaAutoconfMailserverServiceImpl() {

    super( ConfigurationMethod.MOZILLA_AUTOCONF, null, null, null, null );
  }


  @Override
  public String getUsername() {
    return username;
  }


  public void setUsername( String username ) {
    this.username = username;
  }


  @Override
  public String getPassword() {
    return password;
  }


  public void setPassword( String password ) {
    this.password = password;
  }


  @Override
  public Set<Authentication> getAuthentications() {
    return Collections.unmodifiableSet( authentications );
  }


  public void addAuthentication( Authentication authentication ) {
    this.authentications.add( authentication );
  }


  @Override
  public Set<OAuth2> getOAuth2s() {
    return Collections.unmodifiableSet( oAuth2s );
  }


  public void addAllOAuth2s( Collection<OAuth2> oAuth2s ) {
    this.oAuth2s.addAll( oAuth2s );
  }


  @Override
  public String toString() {

    return super.toString() + ", username='" + username + "', password='" + password + "', authentications=" + authentications + ", oAuth2s=" + oAuth2s;
  }
}
