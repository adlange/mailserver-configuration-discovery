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
