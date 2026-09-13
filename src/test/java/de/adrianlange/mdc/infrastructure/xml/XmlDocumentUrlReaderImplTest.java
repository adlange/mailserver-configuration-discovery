package de.adrianlange.mdc.infrastructure.xml;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReaderImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class XmlDocumentUrlReaderImplTest {

  private static final String XML = "<?xml version=\"1.0\"?><clientConfig version=\"1.1\"><emailProvider " + "id=\"x" +
      "\"/></clientConfig>";

  private static final String HTML = "<!DOCTYPE html><html><body>Moved</body></html>";

  private static HttpServer server;

  private static String baseUrl;

  /**
   * Second server on another port, simulating a hosting provider the first server redirects to.
   */
  private static HttpServer otherServer;

  private static String otherBaseUrl;


  @BeforeAll
  static void startServer() throws IOException {

    server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
    server.createContext( "/config.xml", e -> respond( e, 200, "application/xml", XML ) );
    server.createContext( "/not-found", e -> respond( e, 404, "text/html", HTML ) );
    server.createContext( "/html", e -> respond( e, 200, "text/html", HTML ) );
    server.createContext( "/redirect", e -> {
      e.getResponseHeaders().add( "Location", baseUrl + "/config.xml" );
      respond( e, 301, "text/html", HTML );
    } );
    server.createContext( "/redirect-loop", e -> {
      e.getResponseHeaders().add( "Location", baseUrl + "/redirect-loop" );
      respond( e, 301, "text/html", HTML );
    } );
    server.createContext( "/doctype", e -> respond( e, 200, "application/xml", "<?xml version=\"1.0\"?><!DOCTYPE " +
        "clientConfig [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><clientConfig>&x;</clientConfig>" ) );
    server.createContext( "/redirect-other-host", e -> {
      e.getResponseHeaders().add( "Location", otherBaseUrl + "/provider/config.xml?emailaddress=alan%40example.com" );
      respond( e, 301, "text/html", HTML );
    } );
    server.start();
    baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();

    otherServer = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
    otherServer.createContext( "/provider/config.xml", e -> {
      if( "emailaddress=alan%40example.com".equals( e.getRequestURI().getRawQuery() ) )
        respond( e, 200, "application/xml", XML );
      else
        respond( e, 400, "text/html", HTML );
    } );
    otherServer.start();
    otherBaseUrl = "http://127.0.0.1:" + otherServer.getAddress().getPort();
  }


  @AfterAll
  static void stopServer() {

    server.stop( 0 );
    otherServer.stop( 0 );
  }


  private static void respond( HttpExchange exchange, int status, String contentType, String body ) throws IOException {

    var bytes = body.getBytes( StandardCharsets.UTF_8 );
    exchange.getResponseHeaders().add( "Content-Type", contentType );
    exchange.sendResponseHeaders( status, bytes.length );
    try( OutputStream os = exchange.getResponseBody() ) {
      os.write( bytes );
    }
  }


  private static XmlDocumentUrlReaderImpl createReader() {

    return new XmlDocumentUrlReaderImpl( Duration.ofSeconds( 5 ), null );
  }


  @Test
  void testReadsXmlDocument() {

    var document = createReader().getDocument( baseUrl + "/config.xml" );

    assertTrue( document.isPresent() );
    assertEquals( "clientConfig", document.get().getDocumentElement().getNodeName() );
  }


  @Test
  void testFollowsRedirect() {

    var document = createReader().getDocument( baseUrl + "/redirect" );

    assertTrue( document.isPresent() );
    assertEquals( "clientConfig", document.get().getDocumentElement().getNodeName() );
  }


  @Test
  void testFollowsRedirectToOtherHostKeepingQueryParameters() {

    var document = createReader().getDocument( baseUrl + "/redirect-other-host" );

    assertTrue( document.isPresent() );
    assertEquals( "clientConfig", document.get().getDocumentElement().getNodeName() );
  }


  @Test
  void testIgnoresNotFound() {

    assertTrue( createReader().getDocument( baseUrl + "/not-found" ).isEmpty() );
  }


  @Test
  void testIgnoresHtmlDocument() {

    assertTrue( createReader().getDocument( baseUrl + "/html" ).isEmpty() );
  }


  @Test
  void testIgnoresRedirectLoop() {

    assertTrue( createReader().getDocument( baseUrl + "/redirect-loop" ).isEmpty() );
  }


  @Test
  void testRejectsDoctypeDeclaration() {

    assertTrue( createReader().getDocument( baseUrl + "/doctype" ).isEmpty() );
  }


  @Test
  void testIgnoresUnknownHost() {

    assertTrue( createReader().getDocument( "https://autoconfig.dummy-domain.invalid/mail/config-v1.1.xml" ).isEmpty() );
  }


  @Test
  void testRejectsNonHttpSchemes() {

    var reader = createReader();

    assertTrue( reader.getDocument( "file:///etc/passwd" ).isEmpty() );
    assertTrue( reader.getDocument( "jar:file:///tmp/x.jar!/config.xml" ).isEmpty() );
    assertTrue( reader.getDocument( "ftp://example.com/config.xml" ).isEmpty() );
    assertTrue( reader.getDocument( "/relative/path.xml" ).isEmpty() );
    assertTrue( reader.getDocument( "not a url" ).isEmpty() );
    assertTrue( reader.getDocument( null ).isEmpty() );
  }
}
