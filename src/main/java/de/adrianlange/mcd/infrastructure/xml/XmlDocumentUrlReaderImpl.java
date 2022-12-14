package de.adrianlange.mcd.infrastructure.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;


public class XmlDocumentUrlReaderImpl implements XmlDocumentUrlReader {

  private static final Logger LOG = LoggerFactory.getLogger( XmlDocumentUrlReaderImpl.class );


  public XmlDocumentUrlReaderImpl() {
  }


  @Override
  public Optional<Document> getDocument( String url ) {

    if( !url.startsWith( "http" ) )
      return Optional.empty();

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      return Optional.ofNullable( db.parse( new URL( url ).openStream() ) );
    } catch( UnknownHostException uhe ) {
      LOG.debug( "Host not found!", uhe );
    } catch( Exception e ) {
      LOG.error( "Unexpected exception, document {} will be ignored!", url, e );
    }
    return Optional.empty();
  }
}
