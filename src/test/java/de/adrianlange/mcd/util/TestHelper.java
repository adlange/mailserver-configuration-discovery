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
package de.adrianlange.mcd.util;

import de.adrianlange.mcd.model.MailserverService;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class TestHelper {

  private TestHelper() {
  }


  /**
   * Waits for all {@link CompletableFuture} in the result of a
   * {@link de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy} and merges all results into one
   * deduplicated list.
   *
   * @param strategyResult Result of a {@link de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy}
   * @return List of {@link MailserverService}
   */
  public static <T extends MailserverService> List<T> getResultList( List<CompletableFuture<List<T>>> strategyResult ) {

    return strategyResult.stream().map( CompletableFuture::join ).flatMap( List::stream ).distinct().collect( Collectors.toList() );
  }


  /**
   * Returns a {@link Document} from a resource path.
   *
   * @param path Path to file in resource directory, e.g. <code>/foo/bar.xml</code>
   * @return Document
   */
  public static Document readDocumentFromFile( String path ) {

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      return db.parse( TestHelper.class.getResource( path ).getFile() );
    } catch( ParserConfigurationException | SAXException | IOException e ) {
      throw new IllegalStateException( "Could not read test resource " + path, e );
    }
  }

}
