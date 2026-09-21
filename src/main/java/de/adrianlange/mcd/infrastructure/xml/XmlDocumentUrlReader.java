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
package de.adrianlange.mcd.infrastructure.xml;

import org.w3c.dom.Document;

import java.util.Optional;


public interface XmlDocumentUrlReader {

  /**
   * Returns an optional of a DOM {@link Document} object for the given URL.
   *
   * @param url URL to read XML file from
   * @return Optional of Document or empty optional, if URL cannot be read
   */
  Optional<Document> getDocument( String url );
}
