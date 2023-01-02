package de.adrianlange.mcd.strategy.mozillaautoconf;

import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext;
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext.DiscoveryScope;
import de.adrianlange.mcd.infrastructure.dns.TxtDnsResolver;
import de.adrianlange.mcd.infrastructure.dns.TxtDnsResolverImpl;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReader;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReaderImpl;
import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.OAuth2;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.impl.MozillaAutoconfMailserverServiceImpl;
import de.adrianlange.mcd.model.impl.OAuth2Impl;
import de.adrianlange.mcd.strategy.EmailAddress;
import de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xbill.DNS.TXTRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class MozillaAutoconfMailserverConfigurationDiscoveryStrategy implements MailserverConfigurationDiscoveryStrategy {

  private static final Logger LOG =
      LoggerFactory.getLogger( MozillaAutoconfMailserverConfigurationDiscoveryStrategy.class );

  private static final String EL_ROOT = "clientConfig";

  private static final String EL_1_OAUTH2 = "oAuth2";

  private static final String EL_2_ISSUER = "issuer";

  private static final String EL_2_SCOPE = "scope";

  private static final String EL_2_AUTH_URL = "authURL";

  private static final String EL_2_TOKEN_URL = "tokenURL";

  private static final String EL_1_EMAIL_PROVIDER = "emailProvider";

  private static final String EL_2_INCOMING_SERVER = "incomingServer";

  private static final String EL_2_OUTGOING_SERVER = "outgoingServer";

  private static final String EL_3_HOSTNAME = "hostname";

  private static final String EL_3_PORT = "port";

  private static final String EL_3_SOCKET_TYPE = "socketType";

  private static final String EL_3_AUTHENTICATION = "authentication";

  private static final String EL_3_USERNAME = "username";

  private static final String EL_3_PASSWORD = "password";

  private static final String PLACEHOLDER_EMAIL_ADDRESS = "EMAILADDRESS";

  private static final String PLACEHOLDER_EMAIL_LOCAL_PART = "EMAILLOCALPART";

  private static final String PLACEHOLDER_EMAIL_DOMAIN = "EMAILDOMAIN";

  private final MailserverConfigurationDiscoveryContext context;

  private XmlDocumentUrlReader xmlDocumentUrlReader = new XmlDocumentUrlReaderImpl();

  private TxtDnsResolver txtDnsResolver;


  public MozillaAutoconfMailserverConfigurationDiscoveryStrategy( MailserverConfigurationDiscoveryContext context ) {

    this.context = context;
    txtDnsResolver = new TxtDnsResolverImpl( context.getDnsLookupContext() );
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress emailAddress ) {

    var urls = getLookupUrls( emailAddress.getDomainPart().toIdn(), emailAddress.toIdn() );
    var placeholders = getPlaceholders( emailAddress );

    return getCompletableFutures( emailAddress.getDomainPart(), urls, placeholders );
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress.DomainPart domainPart ) {

    var urls = getLookupUrls( domainPart.toIdn(), null );
    var placeholders = getPlaceholders( domainPart );

    return getCompletableFutures( domainPart, urls, placeholders );
  }


  private List<CompletableFuture<List<MailserverService>>> getCompletableFutures( EmailAddress.DomainPart domainPart,
                                                                                  Collection<String> urls, Map<String, String> placeholders ) {

    List<CompletableFuture<List<MailserverService>>> completableFutures = new ArrayList<>();
    // @formatter:off
    urls.stream()
        .map( url -> CompletableFuture.supplyAsync( () -> getMailserverServicesFromUrl( url, placeholders ), context.getExecutor() ) )
        .forEach( completableFutures::add );
    // @formatter:on
    completableFutures.add( getMailserverServicesFromDnsUrl( domainPart.toIdn(), placeholders ) );

    return completableFutures;
  }


  private CompletableFuture<List<MailserverService>> getMailserverServicesFromDnsUrl( String domain, Map<String,
      String> placeholders ) {

    return CompletableFuture.supplyAsync( () -> {
      // @formatter:off
      return txtDnsResolver.getTxtRecords( domain ).stream()
          .map( TXTRecord::getStrings )
          .map( t -> String.join( "", t ) )
          .filter( u -> u.startsWith( "mailconf=" ) )
          .map( u -> u.replaceFirst( "^mailconf=", "" ) )
          .map( u -> getMailserverServicesFromUrl( u, placeholders ) )
          .flatMap( List::stream )
          .toList();
      // @formatter:on

    }, context.getExecutor() );
  }


  private List<MailserverService> getMailserverServicesFromUrl( String url, Map<String, String> placeholders ) {

    // @formatter:off
    return getDocumentFromUrl( url ).map(
        document -> getMailserverServicesFromDocument( document, placeholders ).stream()
            .filter( s -> context.getDiscoveryScopes().contains( DiscoveryScope.get( s.getProtocol() ) ) )
            .toList() )
        .orElse( Collections.emptyList() );
    // @formatter:on
  }


  private static List<MailserverService> getMailserverServicesFromDocument( Document document,
                                                                            Map<String, String> placeholders ) {

    if( !document.getDocumentElement().getNodeName().equals( EL_ROOT ) ) {
      LOG.debug( "Document root {} must equal {}!", document.getDocumentElement().getNodeName(), EL_ROOT );
      return Collections.emptyList();
    }

    var oAuth2s = getOAuth2sFromDocument( document, placeholders );

    return getMailserverServicesFromDocumentElement( document.getDocumentElement(), placeholders, oAuth2s );
  }


  private static List<MailserverService> getMailserverServicesFromDocumentElement( Element documentElement,
                                                                                   Map<String, String> placeholders,
                                                                                   Set<OAuth2> oAuth2s ) {
    // @formatter:off
    return getElementStreamOf( documentElement.getChildNodes() )
        .filter( e -> e.getNodeName().equalsIgnoreCase( EL_1_EMAIL_PROVIDER ) )
        .map( e -> getMailserverServicesFromEmailProvider( e, placeholders, oAuth2s ) )
        .flatMap( List::stream )
        .toList();
    // @formatter:on
  }


  private static List<MailserverService> getMailserverServicesFromEmailProvider( Element emailProviderElement,
                                                                                 Map<String, String> placeholders,
                                                                                 Set<OAuth2> oAuth2s ) {

    // @formatter:off
    return getElementStreamOf( emailProviderElement.getChildNodes() )
        .map( e -> getMailserverServiceFromElement( e, placeholders, oAuth2s ) )
        .filter( Optional::isPresent )
        .map( Optional::get )
        .toList();
    // @formatter:on
  }


  private static Optional<MailserverService> getMailserverServiceFromElement( Element serverElement, Map<String,
      String> placeholders, Set<OAuth2> oAuth2s ) {

    var mailserverService = createMailserverServiceForProtocol( serverElement );
    if( mailserverService == null )
      return Optional.empty();

    getElementStreamOf( serverElement.getChildNodes() ).forEach( c -> {
      if( c.getNodeName().equalsIgnoreCase( EL_3_HOSTNAME ) )
        mailserverService.setHost( replacePlaceholders( c.getTextContent(), placeholders ) );
      else if( c.getNodeName().equalsIgnoreCase( EL_3_PORT ) )
        mailserverService.setPort( Integer.parseInt( c.getTextContent() ) );
      else if( c.getNodeName().equalsIgnoreCase( EL_3_SOCKET_TYPE ) )
        mailserverService.setSocketType( SocketType.parse( c.getTextContent() ) );
      else if( c.getNodeName().equalsIgnoreCase( EL_3_AUTHENTICATION ) )
        mailserverService.addAuthentication( Authentication.parse( c.getTextContent() ) );
      else if( c.getNodeName().equalsIgnoreCase( EL_3_USERNAME ) )
        mailserverService.setUsername( replacePlaceholders( c.getTextContent(), placeholders ) );
      else if( c.getNodeName().equalsIgnoreCase( EL_3_PASSWORD ) )
        mailserverService.setPassword( replacePlaceholders( c.getTextContent(), placeholders ) );
    } );

    mailserverService.addAllOAuth2s( oAuth2s );

    return Optional.of( mailserverService );
  }


  private static MozillaAutoconfMailserverServiceImpl createMailserverServiceForProtocol( Element serverElement ) {

    var protocol = getProtocolFromElement( serverElement );
    if( protocol == null )
      return null;

    var mailserverService = new MozillaAutoconfMailserverServiceImpl();
    mailserverService.setProtocol( protocol );

    return mailserverService;
  }


  private static Protocol getProtocolFromElement( Element serverElement ) {

    if( !serverElement.hasAttribute( "type" ) )
      return null;
    var type = serverElement.getAttribute( "type" );

    if( serverElement.getNodeName().equalsIgnoreCase( EL_2_INCOMING_SERVER ) ) {
      if( type.equalsIgnoreCase( "imap" ) )
        return Protocol.IMAP;
      else if( type.equalsIgnoreCase( "pop3" ) )
        return Protocol.POP3;
    } else if( serverElement.getNodeName().equalsIgnoreCase( EL_2_OUTGOING_SERVER ) && type.equalsIgnoreCase( "smtp" ) ) {
      return Protocol.SMTP;
    }
    return null;
  }


  private static Set<OAuth2> getOAuth2sFromDocument( Document document, Map<String, String> placeholders ) {

    // @formatter:off
    return getElementStreamOf( document.getDocumentElement().getChildNodes() )
        .filter( element -> element.getNodeName().equalsIgnoreCase( EL_1_OAUTH2 ) )
        .map( oauth2Element -> getOAuth2FromElement( oauth2Element, placeholders ) )
        .collect( Collectors.toSet() );
    // @formatter:on
  }


  private static OAuth2 getOAuth2FromElement( Element oauth2Element, Map<String, String> placeholders ) {

    var oAuth2 = new OAuth2Impl();
    getElementStreamOf( oauth2Element.getChildNodes() ).forEach( t -> {
      if( t.getNodeName().equalsIgnoreCase( EL_2_ISSUER ) )
        oAuth2.setIssuer( replacePlaceholders( t.getTextContent(), placeholders ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_2_SCOPE ) )
        oAuth2.setScope( replacePlaceholders( t.getTextContent(), placeholders ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_2_AUTH_URL ) )
        oAuth2.setAuthUrl( replacePlaceholders( t.getTextContent(), placeholders ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_2_TOKEN_URL ) )
        oAuth2.setTokenUrl( replacePlaceholders( t.getTextContent(), placeholders ) );
    } );
    return oAuth2;
  }


  private static Map<String, String> getPlaceholders( EmailAddress emailAddress ) {

    Map<String, String> placeholders = new HashMap<>();
    placeholders.put( PLACEHOLDER_EMAIL_ADDRESS, emailAddress.toUnicode() );
    placeholders.put( PLACEHOLDER_EMAIL_LOCAL_PART, emailAddress.getLocalPart() );
    placeholders.put( PLACEHOLDER_EMAIL_DOMAIN, emailAddress.getDomainPart().toUnicode() );
    return placeholders;
  }


  private static Map<String, String> getPlaceholders( EmailAddress.DomainPart domainPart ) {

    Map<String, String> placeholders = new HashMap<>();
    placeholders.put( PLACEHOLDER_EMAIL_DOMAIN, domainPart.toUnicode() );
    return placeholders;
  }


  private static String replacePlaceholders( String input, Map<String, String> placeholders ) {

    for( Map.Entry<String, String> p : placeholders.entrySet() )
      input = input.replace( "%" + p.getKey() + "%", p.getValue() );
    return input;
  }


  private Optional<Document> getDocumentFromUrl( String url ) {

    return xmlDocumentUrlReader.getDocument( url );
  }


  private Set<String> getLookupUrls( String domain, String emailAddress ) {

    Set<String> urls = new HashSet<>();

    if( emailAddress == null ) {
      urls.add( "http://autoconfig." + domain + "/mail/config-v1.1.xml" );
    } else {
      urls.add( "http://autoconfig." + domain + "/mail/config-v1.1.xml?emailaddress=" + emailAddress );
    }
    urls.add( "http://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml" );

    return urls;
  }


  /**
   * Returns a stream of {@link Element} objects from a {@link NodeList}. Don't use this function if you plan to make
   * changes on the DOM tree.
   *
   * @param nodeList Node list to get a stream of elements from.
   * @return Stream of elements
   */
  private static Stream<Element> getElementStreamOf( NodeList nodeList ) {

    // @formatter:off
    return IntStream
        .range(0, nodeList.getLength())
        .mapToObj(nodeList::item)
        .filter( n -> n.getNodeType() == Node.ELEMENT_NODE )
        .map( Element.class::cast );
    // @formatter:on
  }
}
