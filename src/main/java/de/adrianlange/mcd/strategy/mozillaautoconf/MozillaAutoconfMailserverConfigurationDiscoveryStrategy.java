package de.adrianlange.mcd.strategy.mozillaautoconf;

import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext;
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
  public List<MailserverService> getMailserverServices( EmailAddress emailAddress ) {

    // @formatter:off
    return getDocuments( emailAddress.getDomainPart().toIdn(), emailAddress.toIdn() ).stream()
        .map( d -> getMailserverServicesFromDocument( d, emailAddress ) )
        .flatMap( List::stream )
        .toList();
    // @formatter:on
  }


  private List<MailserverService> getMailserverServicesFromDocument( Document document, EmailAddress emailAddress ) {

    if( !document.getDocumentElement().getNodeName().equals( EL_ROOT ) ) {
      LOG.debug( "Document root {} must equal {}!", document.getDocumentElement().getNodeName(), EL_ROOT );
      return Collections.emptyList();
    }

    var placeholders = getPlaceholders( emailAddress );
    var oAuth2s = getOAuth2sFromDocument( document, placeholders );

    return getMailserverServices( document.getDocumentElement(), placeholders, oAuth2s );
  }


  private static List<MailserverService> getMailserverServices( Element documentElement,
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


  private static List<MailserverService> getMailserverServicesFromEmailProvider( Element emailProvider, Map<String,
      String> placeholders, Set<OAuth2> oAuth2s ) {

    // @formatter:off
    return getElementStreamOf( emailProvider.getChildNodes() )
        .map( e -> getMailserverService( e, placeholders, oAuth2s ) )
        .filter( Optional::isPresent )
        .map( Optional::get )
        .toList();
    // @formatter:on
  }


  private static Optional<MailserverService> getMailserverService( Element element, Map<String, String> placeholders,
                                                                   Set<OAuth2> oAuth2s ) {

    var mailserverService = createMozillaAutoconfMailserverService( element );
    if( mailserverService == null )
      return Optional.empty();

    getElementStreamOf( element.getChildNodes() ).forEach( t -> {
      if( t.getNodeName().equalsIgnoreCase( EL_3_HOSTNAME ) )
        mailserverService.setHost( replacePlaceholders( t.getTextContent(), placeholders ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_3_PORT ) )
        mailserverService.setPort( Integer.parseInt( t.getTextContent() ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_3_SOCKET_TYPE ) )
        mailserverService.setSocketType( SocketType.parse( t.getTextContent() ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_3_AUTHENTICATION ) )
        mailserverService.addAuthentication( Authentication.parse( t.getTextContent() ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_3_USERNAME ) )
        mailserverService.setUsername( replacePlaceholders( t.getTextContent(), placeholders ) );
      else if( t.getNodeName().equalsIgnoreCase( EL_3_PASSWORD ) )
        mailserverService.setPassword( replacePlaceholders( t.getTextContent(), placeholders ) );
    } );

    mailserverService.addAllOAuth2s( oAuth2s );

    return Optional.of( mailserverService );
  }


  private static MozillaAutoconfMailserverServiceImpl createMozillaAutoconfMailserverService( Element element ) {

    var protocol = getProtocolFromElement( element );
    if( protocol == null )
      return null;

    var mailserverService = new MozillaAutoconfMailserverServiceImpl();
    mailserverService.setProtocol( protocol );

    return mailserverService;
  }


  private static Protocol getProtocolFromElement( Element element ) {

    if( !element.hasAttribute( "type" ) )
      return null;
    var type = element.getAttribute( "type" );

    if( element.getNodeName().equalsIgnoreCase( EL_2_INCOMING_SERVER ) ) {
      if( type.equalsIgnoreCase( "imap" ) )
        return Protocol.IMAP;
      else if( type.equalsIgnoreCase( "pop3" ) )
        return Protocol.POP3;
    } else if( element.getNodeName().equalsIgnoreCase( EL_2_OUTGOING_SERVER ) && type.equalsIgnoreCase( "smtp" ) ) {
      return Protocol.SMTP;
    }
    return null;
  }


  private static Set<OAuth2> getOAuth2sFromDocument( Document document, Map<String, String> placeholders ) {

    // @formatter:off
    return getElementStreamOf( document.getDocumentElement().getChildNodes() )
        .filter( e -> e.getNodeName().equalsIgnoreCase( EL_1_OAUTH2 ) )
        .map( e -> getOAuth2FromElement( e, placeholders ) )
        .collect( Collectors.toSet() );
    // @formatter:on
  }


  private static OAuth2 getOAuth2FromElement( Element element, Map<String, String> placeholders ) {

    var oAuth2 = new OAuth2Impl();
    getElementStreamOf( element.getChildNodes() ).forEach( t -> {
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


  private List<Document> getDocuments( String domain, String emailAddress ) {

    List<Document> documents = new ArrayList<>();
    var urls = getLookupUrls( domain, emailAddress );
    for( var url : urls ) {
      xmlDocumentUrlReader.getDocument( url ).ifPresent( documents::add );
    }

    return documents;
  }


  private Set<String> getLookupUrls( String domain, String emailAddress ) {

    Set<String> urls = new HashSet<>();

    if( emailAddress == null ) {
      urls.add( "http://autoconfig." + domain + "/mail/config-v1.1.xml" );
    } else {
      urls.add( "http://autoconfig." + domain + "/mail/config-v1.1.xml?emailaddress=" + emailAddress );
    }
    urls.add( "http://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml" );

    // @formatter:off
    var urlsFromDns = txtDnsResolver.getTxtRecords( domain ).stream()
        .map( TXTRecord::getStrings )
        .map( t -> String.join( "", t ) )
        .filter( u -> u.startsWith( "mailconf=" ) )
        .map( u -> u.replaceFirst( "^mailconf=", "" ) )
        .collect( Collectors.toSet() );
    // @formatter:on
    urls.addAll( urlsFromDns );

    return urls;
  }


  @Override
  public List<MailserverService> getMailserverServices( EmailAddress.DomainPart domainPart ) {
    return null;
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServicesAsync( EmailAddress emailAddress ) {
    return null;
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServicesAsync( EmailAddress.DomainPart domainPart ) {
    return null;
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
