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
 * Representation of a OAuth2 configuration.<p>May contain placeholders, see {@link MozillaAutoconfigMailserverService}
 * for more information.
 *
 * @author Adrian Lange
 */
public interface OAuth2 {

  /**
   * Returns the token issuer.<p>May contain placeholders, see {@link MozillaAutoconfigMailserverService} for more
   * information.
   *
   * @return Name of the issuer or null if not set.
   */
  String getIssuer();


  /**
   * Returns the token scope.<p>May contain placeholders, see {@link MozillaAutoconfigMailserverService} for more
   * information.
   *
   * @return Name of the scope or null if not set.
   */
  String getScope();


  /**
   * Returns the auth URL.<p>May contain placeholders, see {@link MozillaAutoconfigMailserverService} for more
   * information.
   *
   * @return Auth URL or null if not set.
   */
  String getAuthUrl();


  /**
   * Returns the token URL.<p>May contain placeholders, see {@link MozillaAutoconfigMailserverService} for more
   * information.
   *
   * @return Token URL or null if not set.
   */
  String getTokenUrl();
}
