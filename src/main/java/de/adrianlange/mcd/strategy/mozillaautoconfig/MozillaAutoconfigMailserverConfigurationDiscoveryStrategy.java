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
package de.adrianlange.mcd.strategy.mozillaautoconfig;

import de.adrianlange.mcd.EmailAddress;
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext;
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext.DiscoveryScope;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReader;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReaderImpl;
import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.OAuth2;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.impl.MozillaAutoconfigMailserverServiceImpl;
import de.adrianlange.mcd.model.impl.OAuth2Impl;
import de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class MozillaAutoconfigMailserverConfigurationDiscoveryStrategy implements MailserverConfigurationDiscoveryStrategy {

  private static final Logger LOG =
      LoggerFactory.getLogger( MozillaAutoconfigMailserverConfigurationDiscoveryStrategy.class );

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

  private final XmlDocumentUrlReader xmlDocumentUrlReader;


  public MozillaAutoconfigMailserverConfigurationDiscoveryStrategy( MailserverConfigurationDiscoveryContext context ) {

    this( context, new XmlDocumentUrlReaderImpl( context.getHttpTimeout(), context.getExecutor() ) );
  }


  /**
   * Constructor for tests, allows injecting the document reader.
   */
  MozillaAutoconfigMailserverConfigurationDiscoveryStrategy( MailserverConfigurationDiscoveryContext context,
                                                             XmlDocumentUrlReader xmlDocumentUrlReader ) {

    this.context = context;
    this.xmlDocumentUrlReader = xmlDocumentUrlReader;
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress emailAddress ) {

    var urls = getLookupUrls( emailAddress.getDomainPart().toIdn(), emailAddress.toIdn() );
    var placeholders = getPlaceholders( emailAddress );

    return getCompletableFutures( urls, placeholders );
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress.DomainPart domainPart ) {

    var urls = getLookupUrls( domainPart.toIdn(), null );
    var placeholders = getPlaceholders( domainPart );

    return getCompletableFutures( urls, placeholders );
  }


  private List<CompletableFuture<List<MailserverService>>> getCompletableFutures( Collection<String> urls, Map<String
      , String> placeholders ) {

    // @formatter:off
    return urls.stream()
        .map( url -> CompletableFuture.supplyAsync( () -> getMailserverServicesFromUrl( url, placeholders ), context.getExecutor() ) )
        .collect( Collectors.toCollection( ArrayList::new ) );
    // @formatter:on
  }


  private List<MailserverService> getMailserverServicesFromUrl( String url, Map<String, String> placeholders ) {

    // @formatter:off
    return getDocumentFromUrl( url ).map(
        document -> getMailserverServicesFromDocument( document, placeholders ).stream()
            .filter( s -> context.getDiscoveryScopes().contains( DiscoveryScope.of( s.getProtocol() ) ) )
            .toList() )
        .orElse( Collections.emptyList() );
    // @formatter:on
  }


  private static List<MailserverService> getMailserverServicesFromDocument( Document document,
                                                                            Map<String, String> placeholders ) {

    if( !document.getDocumentElement().getNodeName().equalsIgnoreCase( EL_ROOT ) ) {
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

    for( Element c : getElementStreamOf( serverElement.getChildNodes() ).toList() ) {
      var text = c.getTextContent();
      if( c.getNodeName().equalsIgnoreCase( EL_3_HOSTNAME ) ) {
        mailserverService.setHost( replacePlaceholders( text, placeholders ) );
      } else if( c.getNodeName().equalsIgnoreCase( EL_3_PORT ) ) {
        var port = parsePort( text );
        if( port == null ) {
          LOG.debug( "Ignoring {} {} because of invalid port '{}'", serverElement.getNodeName(),
              serverElement.getAttribute( "type" ), text );
          return Optional.empty();
        }
        mailserverService.setPort( port );
      } else if( c.getNodeName().equalsIgnoreCase( EL_3_SOCKET_TYPE ) ) {
        var socketType = SocketType.parse( text );
        if( socketType == null )
          LOG.debug( "Ignoring unknown socket type '{}'", text );
        mailserverService.setSocketType( socketType );
      } else if( c.getNodeName().equalsIgnoreCase( EL_3_AUTHENTICATION ) ) {
        var authentication = Authentication.parse( text );
        if( authentication == null )
          LOG.debug( "Ignoring unknown authentication method '{}'", text );
        else
          mailserverService.addAuthentication( authentication );
      } else if( c.getNodeName().equalsIgnoreCase( EL_3_USERNAME ) ) {
        mailserverService.setUsername( replacePlaceholders( text, placeholders ) );
      } else if( c.getNodeName().equalsIgnoreCase( EL_3_PASSWORD ) ) {
        mailserverService.setPassword( replacePlaceholders( text, placeholders ) );
      }
    }

    mailserverService.addAllOAuth2s( oAuth2s );

    return Optional.of( mailserverService );
  }


  private static Integer parsePort( String text ) {

    try {
      var port = Integer.parseInt( text.trim() );
      return port >= 1 && port <= 65535 ? port : null;
    } catch( NumberFormatException e ) {
      return null;
    }
  }


  private static MozillaAutoconfigMailserverServiceImpl createMailserverServiceForProtocol( Element serverElement ) {

    var protocol = getProtocolFromElement( serverElement );
    if( protocol == null )
      return null;

    var mailserverService = new MozillaAutoconfigMailserverServiceImpl();
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

    Set<String> urls = new LinkedHashSet<>();

    urls.addAll( getLookupUrls( "https", domain, emailAddress ) );
    if( context.isInsecureHttpAllowed() )
      urls.addAll( getLookupUrls( "http", domain, emailAddress ) );

    return urls;
  }


  private static List<String> getLookupUrls( String scheme, String domain, String emailAddress ) {

    var autoconfigUrl = scheme + "://autoconfig." + domain + "/mail/config-v1.1.xml";
    if( emailAddress != null )
      autoconfigUrl += "?emailaddress=" + URLEncoder.encode( emailAddress, StandardCharsets.UTF_8 );

    var wellKnownUrl = scheme + "://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml";

    return List.of( autoconfigUrl, wellKnownUrl );
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
