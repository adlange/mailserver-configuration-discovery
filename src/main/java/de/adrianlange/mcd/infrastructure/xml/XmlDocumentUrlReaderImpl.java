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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;


public class XmlDocumentUrlReaderImpl implements XmlDocumentUrlReader {

  private static final Logger LOG = LoggerFactory.getLogger( XmlDocumentUrlReaderImpl.class );

  private static final Set<String> ALLOWED_SCHEMES = Set.of( "http", "https" );

  private static final ErrorHandler QUIET_ERROR_HANDLER = new ErrorHandler() {

    @Override
    public void warning( SAXParseException exception ) {
      // ignore
    }


    @Override
    public void error( SAXParseException exception ) throws SAXException {
      throw exception;
    }


    @Override
    public void fatalError( SAXParseException exception ) throws SAXException {
      throw exception;
    }
  };

  private final HttpClient httpClient;

  private final Duration timeout;


  public XmlDocumentUrlReaderImpl( Duration timeout, Executor executor ) {

    if( timeout == null )
      throw new IllegalArgumentException( "Timeout must not be null!" );

    this.timeout = timeout;

    var builder = HttpClient.newBuilder().followRedirects( HttpClient.Redirect.NORMAL ).connectTimeout( timeout );
    if( executor != null )
      builder.executor( executor );
    this.httpClient = builder.build();
  }


  @Override
  public Optional<Document> getDocument( String url ) {

    var uri = toAllowedUri( url );
    if( uri.isEmpty() )
      return Optional.empty();

    var request = HttpRequest.newBuilder( uri.get() ).timeout( timeout ).header( "Accept", "application/xml, " +
        "text/xml;q=0.9, */*;q=0.1" ).GET().build();

    try {
      var response = httpClient.send( request, HttpResponse.BodyHandlers.ofInputStream() );
      try( InputStream body = response.body() ) {
        if( response.statusCode() != 200 ) {
          LOG.debug( "Document {} returned HTTP status {}, it will be ignored", url, response.statusCode() );
          return Optional.empty();
        }
        return Optional.ofNullable( createDocumentBuilder().parse( body ) );
      }
    } catch( SAXException e ) {
      LOG.debug( "Document {} is not valid XML, it will be ignored: {}", url, e.getMessage() );
    } catch( IOException e ) {
      // covers UnknownHostException, ConnectException, HttpTimeoutException, ... - all expected during discovery
      LOG.debug( "Document {} could not be fetched, it will be ignored: {}", url, e.toString() );
    } catch( InterruptedException e ) {
      Thread.currentThread().interrupt();
      LOG.debug( "Interrupted while fetching document {}", url );
    } catch( ParserConfigurationException e ) {
      LOG.error( "XML parser could not be configured, document {} will be ignored!", url, e );
    }
    return Optional.empty();
  }


  private static Optional<URI> toAllowedUri( String url ) {

    if( url == null )
      return Optional.empty();

    try {
      var uri = URI.create( url.trim() );
      var scheme = uri.getScheme();
      if( scheme == null || !ALLOWED_SCHEMES.contains( scheme.toLowerCase( Locale.ROOT ) ) || uri.getHost() == null ) {
        LOG.debug( "URL {} is not an absolute http(s) URL, it will be ignored", url );
        return Optional.empty();
      }
      return Optional.of( uri );
    } catch( IllegalArgumentException e ) {
      LOG.debug( "URL {} is invalid, it will be ignored: {}", url, e.getMessage() );
      return Optional.empty();
    }
  }


  private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    dbf.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
    dbf.setXIncludeAware( false );
    dbf.setExpandEntityReferences( false );
    var documentBuilder = dbf.newDocumentBuilder();
    // the default handler prints "[Fatal Error]" to stderr; non-XML responses are expected during discovery
    documentBuilder.setErrorHandler( QUIET_ERROR_HANDLER );
    return documentBuilder;
  }
}
