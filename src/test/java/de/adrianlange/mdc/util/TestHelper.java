package de.adrianlange.mdc.util;

import de.adrianlange.mcd.model.MailserverService;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Field;
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


  /**
   * Injects a value into a private field, replacing the implementation created in the constructor with a mock.
   *
   * @param target    Object to modify
   * @param fieldName Name of the declared field
   * @param value     Value to inject
   */
  public static void setField( Object target, String fieldName, Object value ) {

    try {
      Field field = target.getClass().getDeclaredField( fieldName );
      field.setAccessible( true );
      field.set( target, value );
    } catch( ReflectiveOperationException e ) {
      throw new IllegalStateException( "Could not set field " + fieldName, e );
    }
  }
}
