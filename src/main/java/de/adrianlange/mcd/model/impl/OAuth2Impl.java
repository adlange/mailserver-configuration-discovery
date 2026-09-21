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
