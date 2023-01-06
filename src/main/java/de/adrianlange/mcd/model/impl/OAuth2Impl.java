package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.OAuth2;

import java.util.Objects;


public class OAuth2Impl implements OAuth2 {

  private String issuer = null;

  private String scope = null;

  private String authUrl = null;

  private String tokenUrl = null;


  public OAuth2Impl() {
  }


  protected OAuth2Impl( String issuer, String scope, String authUrl, String tokenUrl ) {

    this.issuer = issuer;
    this.scope = scope;
    this.authUrl = authUrl;
    this.tokenUrl = tokenUrl;
  }


  @Override
  public String getIssuer() {
    return issuer;
  }


  public void setIssuer( String issuer ) {
    this.issuer = issuer;
  }


  @Override
  public String getScope() {
    return scope;
  }


  public void setScope( String scope ) {
    this.scope = scope;
  }


  @Override
  public String getAuthUrl() {
    return authUrl;
  }


  public void setAuthUrl( String authUrl ) {
    this.authUrl = authUrl;
  }


  @Override
  public String getTokenUrl() {
    return tokenUrl;
  }


  public void setTokenUrl( String tokenUrl ) {
    this.tokenUrl = tokenUrl;
  }


  @Override
  public String toString() {

    return "issuer='" + issuer + "', scope='" + scope + "', authUrl='" + authUrl + "', " + "tokenUrl='" + tokenUrl +
        "'";
  }


  @Override
  public boolean equals( Object o ) {
    if( this == o )
      return true;
    if( o == null || getClass() != o.getClass() )
      return false;
    OAuth2Impl oAuth2 = (OAuth2Impl) o;
    return Objects.equals( issuer, oAuth2.issuer ) && Objects.equals( scope, oAuth2.scope ) && Objects.equals( authUrl, oAuth2.authUrl ) && Objects.equals( tokenUrl, oAuth2.tokenUrl );
  }


  @Override
  public int hashCode() {
    return Objects.hash( issuer, scope, authUrl, tokenUrl );
  }
}
