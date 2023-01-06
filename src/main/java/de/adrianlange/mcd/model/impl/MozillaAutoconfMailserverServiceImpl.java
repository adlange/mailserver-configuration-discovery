package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MozillaAutoconfMailserverService;
import de.adrianlange.mcd.model.OAuth2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
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

    var sb = new StringBuilder( super.toString() );
    if( username != null )
      sb.append( ", username='" ).append( username ).append( "'" );
    if( password != null )
      sb.append( ", password='" ).append( password ).append( "'" );
    if( !authentications.isEmpty() )
      sb.append( ", authentications=" ).append( authentications );
    if( !oAuth2s.isEmpty() )
      sb.append( ", oAuth2s=" ).append( oAuth2s );
    return sb.toString();
  }


  @Override
  public boolean equals( Object o ) {
    if( this == o )
      return true;
    if( o == null || getClass() != o.getClass() )
      return false;
    if( !super.equals( o ) )
      return false;
    MozillaAutoconfMailserverServiceImpl that = (MozillaAutoconfMailserverServiceImpl) o;
    return Objects.equals( username, that.username ) && Objects.equals( password, that.password ) && Objects.equals( authentications, that.authentications ) && Objects.equals( oAuth2s, that.oAuth2s );
  }


  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), username, password, authentications, oAuth2s );
  }
}
