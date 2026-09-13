package de.adrianlange.mcd.strategy.mozillaautoconfig;

import de.adrianlange.mcd.EmailAddress;
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReader;
import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.MozillaAutoconfigMailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.util.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


class MozillaAutoconfigMailserverConfigurationDiscoveryStrategyDomainTest {

  private static final String DOMAIN = "example.com";

  private static final String AUTOCONF_URL_1A = "https://autoconfig.%s/mail/config-v1.1.xml";

  private static final String AUTOCONF_URL_1B = "https://autoconfig.%s/mail/config-v1.1.xml?emailaddress=%s";

  private static final String AUTOCONF_URL_2 = "https://%s/.well-known/autoconfig/mail/config-v1.1.xml";

  private static final String INSECURE_AUTOCONF_URL_1A = "http://autoconfig.%s/mail/config-v1.1.xml";

  private static final String INSECURE_AUTOCONF_URL_2 = "http://%s/.well-known/autoconfig/mail/config-v1.1.xml";

  private static final String MOCK_MOZILLA_EXAMPLE = "/autoconfig/mozilla-example.xml";

  private static final String MOCK_SIMPLE = "/autoconfig/simple.xml";

  private static final String MOCK_OAUTH2 = "/autoconfig/oauth2.xml";


  @Test
  void testNoAutoconfDocumentExist() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.empty() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertTrue( configs.isEmpty() );
  }


  @Test
  void testOneAutoconfDocumentExist1A() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.empty() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertEquals( 2, configs.size() );

    assertMozillaDefaultSmtp( findOne( configs, Protocol.SMTP ) );
    assertMozillaDefaultPop3( findOne( configs, Protocol.POP3 ) );
  }


  @Test
  void testOneAutoconfDocumentExist2() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertEquals( 2, configs.size() );

    assertMozillaDefaultSmtp( findOne( configs, Protocol.SMTP ) );
    assertMozillaDefaultPop3( findOne( configs, Protocol.POP3 ) );
  }


  @Test
  void testMultipleEqualConfigDocumentsWillBeMerged() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertEquals( 2, configs.size() );

    var smtps = findAll( configs, Protocol.SMTP );
    var pop3s = findAll( configs, Protocol.POP3 );
    assertEquals( 1, smtps.size() );
    assertEquals( 1, pop3s.size() );
    assertMozillaDefaultSmtp( smtps.get( 0 ) );
    assertMozillaDefaultPop3( pop3s.get( 0 ) );
  }


  @Test
  void testMultipleDifferentConfigDocumentsAreFound() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_SIMPLE ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertEquals( 4, configs.size() );

    var smtpGoogle =
        findAll( configs, Protocol.SMTP ).stream().filter( s -> s.getHost().contains( "google" ) ).toList();
    var smtpExample =
        findAll( configs, Protocol.SMTP ).stream().filter( s -> s.getHost().contains( "example" ) ).toList();
    var pop3 = findAll( configs, Protocol.POP3 );
    var imap = findAll( configs, Protocol.IMAP );

    assertEquals( 1, smtpGoogle.size() );
    assertEquals( 1, smtpExample.size() );
    assertEquals( 1, pop3.size() );
    assertEquals( 1, imap.size() );
    assertMozillaDefaultSmtp( smtpGoogle.get( 0 ) );
    assertSimpleSmtp( smtpExample.get( 0 ) );
    assertMozillaDefaultPop3( pop3.get( 0 ) );
    assertSimpleImap( imap.get( 0 ) );
  }


  @Test
  void testReadingDocumentWithOAuth2Information() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_OAUTH2 ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertEquals( 2, configs.size() );

    assertOAuth2Smtp( findOne( configs, Protocol.SMTP ) );
    assertOAuth2Imap( findOne( configs, Protocol.IMAP ) );
  }


  @Test
  void testHttpUrlsAreNotQueriedByDefault() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );

    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( anyString() ) ).thenReturn( Optional.empty() );

    TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( INSECURE_AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( INSECURE_AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
  }


  @Test
  void testHttpUrlsAreQueriedAdditionallyIfInsecureHttpIsAllowed() {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONFIG ).withInsecureHttpAllowed( true ).build();
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    var strategy = new MozillaAutoconfigMailserverConfigurationDiscoveryStrategy( context, xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( anyString() ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( INSECURE_AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_SIMPLE ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( INSECURE_AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( INSECURE_AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
    assertEquals( 2, configs.size() );
    assertSimpleSmtp( findOne( configs, Protocol.SMTP ) );
    assertSimpleImap( findOne( configs, Protocol.IMAP ) );
  }


  @Test
  void testServerWithInvalidPortIsSkippedButOthersAreKept() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( anyString() ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( "/autoconfig/invalid-port.xml" ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    // IMAP has a non-numeric port, POP3 a port above 65535: both are skipped, SMTP with "465" is kept
    assertEquals( 1, configs.size() );
    var smtp = findOne( configs, Protocol.SMTP );
    assertEquals( "smtp.example.com", smtp.getHost() );
    assertEquals( 465, smtp.getPort() );
  }


  @Test
  void testUnknownAuthenticationAndSocketTypeAreIgnored() {

    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    var strategy = createStrategy( xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( anyString() ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( "/autoconfig/unknown-authentication.xml" ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    assertEquals( 2, configs.size() );
    var imap = findOne( configs, Protocol.IMAP );
    assertNull( imap.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), imap.getAuthentications() );
    var smtp = findOne( configs, Protocol.SMTP );
    assertEquals( SocketType.STARTTLS, smtp.getSocketType() );
    assertTrue( smtp.getAuthentications().isEmpty() );
    assertFalse( smtp.getAuthentications().contains( null ) );
  }


  private static MozillaAutoconfigMailserverConfigurationDiscoveryStrategy createStrategy( XmlDocumentUrlReader xmlDocumentUrlReader ) {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONFIG ).build();
    return new MozillaAutoconfigMailserverConfigurationDiscoveryStrategy( context, xmlDocumentUrlReader );
  }


  private static MozillaAutoconfigMailserverService findOne( List<MailserverService> configs, Protocol protocol ) {

    return findAll( configs, protocol ).stream().findFirst().orElseThrow();
  }


  private static List<MozillaAutoconfigMailserverService> findAll( List<MailserverService> configs,
                                                                   Protocol protocol ) {

    return configs.stream().filter( c -> c.getProtocol() == protocol ).map( MozillaAutoconfigMailserverService.class::cast ).toList();
  }


  private static void assertMozillaDefaultSmtp( MozillaAutoconfigMailserverService smtp ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, smtp.getConfigurationMethod() );
    assertEquals( Protocol.SMTP, smtp.getProtocol() );
    assertEquals( "smtp.googlemail.com", smtp.getHost() );
    assertEquals( 587, smtp.getPort() );
    assertEquals( "%EMAILLOCALPART%", smtp.getUsername() );
    assertEquals( "optional: the user's password", smtp.getPassword() );
    assertEquals( SocketType.STARTTLS, smtp.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), smtp.getAuthentications() );
    assertTrue( smtp.getOAuth2s().isEmpty() );
  }


  private static void assertSimpleSmtp( MozillaAutoconfigMailserverService smtp ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, smtp.getConfigurationMethod() );
    assertEquals( Protocol.SMTP, smtp.getProtocol() );
    assertEquals( "smtp.example.com", smtp.getHost() );
    assertEquals( 465, smtp.getPort() );
    assertNull( smtp.getUsername() );
    assertNull( smtp.getPassword() );
    assertEquals( SocketType.SSL, smtp.getSocketType() );
    assertEquals( Set.of( Authentication.CLIENT_IP_ADDRESS ), smtp.getAuthentications() );
    assertTrue( smtp.getOAuth2s().isEmpty() );
  }


  private static void assertOAuth2Smtp( MozillaAutoconfigMailserverService smtp ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, smtp.getConfigurationMethod() );
    assertEquals( Protocol.SMTP, smtp.getProtocol() );
    assertEquals( "smtp.example.com", smtp.getHost() );
    assertEquals( 465, smtp.getPort() );
    assertEquals( "%EMAILADDRESS%", smtp.getUsername() );
    assertNull( smtp.getPassword() );
    assertEquals( SocketType.SSL, smtp.getSocketType() );
    assertEquals( Set.of( Authentication.OAUTH2, Authentication.PASSWORD_CLEARTEXT ), smtp.getAuthentications() );
    assertOAuth2Details( smtp );
  }


  private static void assertMozillaDefaultPop3( MozillaAutoconfigMailserverService pop3 ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, pop3.getConfigurationMethod() );
    assertEquals( Protocol.POP3, pop3.getProtocol() );
    assertEquals( "pop.example.com", pop3.getHost() );
    assertEquals( 995, pop3.getPort() );
    assertEquals( "%EMAILLOCALPART%", pop3.getUsername() );
    assertEquals( "optional: the user's password", pop3.getPassword() );
    assertEquals( SocketType.SSL, pop3.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), pop3.getAuthentications() );
    assertTrue( pop3.getOAuth2s().isEmpty() );
  }


  private static void assertSimpleImap( MozillaAutoconfigMailserverService imap ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, imap.getConfigurationMethod() );
    assertEquals( Protocol.IMAP, imap.getProtocol() );
    assertEquals( "imap.example.com", imap.getHost() );
    assertEquals( 993, imap.getPort() );
    assertEquals( "%EMAILLOCALPART%", imap.getUsername() );
    assertNull( imap.getPassword() );
    assertEquals( SocketType.SSL, imap.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), imap.getAuthentications() );
    assertTrue( imap.getOAuth2s().isEmpty() );
  }


  private static void assertOAuth2Imap( MozillaAutoconfigMailserverService imap ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, imap.getConfigurationMethod() );
    assertEquals( Protocol.IMAP, imap.getProtocol() );
    assertEquals( "imap.example.com", imap.getHost() );
    assertEquals( 993, imap.getPort() );
    assertEquals( "%EMAILADDRESS%", imap.getUsername() );
    assertNull( imap.getPassword() );
    assertEquals( SocketType.SSL, imap.getSocketType() );
    assertEquals( Set.of( Authentication.OAUTH2, Authentication.PASSWORD_CLEARTEXT ), imap.getAuthentications() );
    assertOAuth2Details( imap );
  }


  private static void assertOAuth2Details( MozillaAutoconfigMailserverService service ) {

    assertEquals( 1, service.getOAuth2s().size() );
    var oAuth2 = service.getOAuth2s().iterator().next();
    assertEquals( "login.yahoo.com", oAuth2.getIssuer() );
    assertEquals( "mail-w", oAuth2.getScope() );
    assertEquals( "https://api.login.yahoo.com/oauth2/request_auth", oAuth2.getAuthUrl() );
    assertEquals( "https://api.login.yahoo.com/oauth2/get_token", oAuth2.getTokenUrl() );
  }
}
