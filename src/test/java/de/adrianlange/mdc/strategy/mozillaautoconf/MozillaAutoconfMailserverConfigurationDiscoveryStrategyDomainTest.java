package de.adrianlange.mdc.strategy.mozillaautoconf;

import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder;
import de.adrianlange.mcd.infrastructure.dns.TxtDnsResolver;
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReader;
import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.MozillaAutoconfMailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.strategy.EmailAddress;
import de.adrianlange.mcd.strategy.mozillaautoconf.MozillaAutoconfMailserverConfigurationDiscoveryStrategy;
import de.adrianlange.mdc.util.DnsHelper;
import de.adrianlange.mdc.util.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


class MozillaAutoconfMailserverConfigurationDiscoveryStrategyDomainTest {

  private static final String DOMAIN = "example.com";

  private static final String AUTOCONF_URL_1A = "https://autoconfig.%s/mail/config-v1.1.xml";

  private static final String AUTOCONF_URL_1B = "https://autoconfig.%s/mail/config-v1.1.xml?emailaddress=%s";

  private static final String AUTOCONF_URL_2 = "https://%s/.well-known/autoconfig/mail/config-v1.1.xml";

  private static final String AUTOCONF_URL_3 = "https://dummy-domain.invalid/autoconfig.xml";

  private static final String INSECURE_AUTOCONF_URL_1A = "http://autoconfig.%s/mail/config-v1.1.xml";

  private static final String INSECURE_AUTOCONF_URL_2 = "http://%s/.well-known/autoconfig/mail/config-v1.1.xml";

  private static final String MOCK_MOZILLA_EXAMPLE = "/autoconf/mozilla-example.xml";

  private static final String MOCK_SIMPLE = "/autoconf/simple.xml";

  private static final String MOCK_OAUTH2 = "/autoconf/oauth2.xml";


  @Test
  void testNoAutoconfDocumentExist() {

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
    assertTrue( configs.isEmpty() );
  }


  @Test
  void testOneAutoconfDocumentExist1A() {

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
    assertEquals( 2, configs.size() );

    assertMozillaDefaultSmtp( findOne( configs, Protocol.SMTP ) );
    assertMozillaDefaultPop3( findOne( configs, Protocol.POP3 ) );
  }


  @Test
  void testOneAutoconfDocumentExist2() {

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
    assertEquals( 2, configs.size() );

    assertMozillaDefaultSmtp( findOne( configs, Protocol.SMTP ) );
    assertMozillaDefaultPop3( findOne( configs, Protocol.POP3 ) );
  }


  @Test
  void testOneAutoconfDocumentExist3() {

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( AUTOCONF_URL_3 ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of( DnsHelper.createTXTRecord( DOMAIN,
        AUTOCONF_URL_3 ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( AUTOCONF_URL_3 );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
    assertEquals( 2, configs.size() );

    assertMozillaDefaultSmtp( findOne( configs, Protocol.SMTP ) );
    assertMozillaDefaultPop3( findOne( configs, Protocol.POP3 ) );
  }


  @Test
  void testMultipleEqualConfigDocumentsWillBeMerged() {

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( AUTOCONF_URL_3 ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of( DnsHelper.createTXTRecord( DOMAIN,
        AUTOCONF_URL_3 ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( AUTOCONF_URL_3 );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
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

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) ) );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_SIMPLE ) ) );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( AUTOCONF_URL_3 );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
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

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) ).thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_OAUTH2 ) ) );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( AUTOCONF_URL_3 );
    verify( txtDnsResolver ).getTxtRecords( DOMAIN );
    verifyNoMoreInteractions( xmlDocumentUrlReader, txtDnsResolver );
    assertEquals( 2, configs.size() );

    assertOAuth2Smtp( findOne( configs, Protocol.SMTP ) );
    assertOAuth2Imap( findOne( configs, Protocol.IMAP ) );
  }


  @Test
  void testHttpUrlsAreNotQueriedByDefault() {

    var strategy = createStrategy();
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( anyString() ) ).thenReturn( Optional.empty() );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

    TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader ).getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( INSECURE_AUTOCONF_URL_1A, DOMAIN ) );
    verify( xmlDocumentUrlReader, never() ).getDocument( String.format( INSECURE_AUTOCONF_URL_2, DOMAIN ) );
    verifyNoMoreInteractions( xmlDocumentUrlReader );
  }


  @Test
  void testHttpUrlsAreQueriedAdditionallyIfInsecureHttpIsAllowed() {

    var context = new MailserverConfigurationDiscoveryContextBuilder()
        .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
        .withInsecureHttpAllowed( true )
        .build();
    var strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context );
    var txtDnsResolver = mock( TxtDnsResolver.class );
    TestHelper.setField( strategy, "txtDnsResolver", txtDnsResolver );
    var xmlDocumentUrlReader = mock( XmlDocumentUrlReader.class );
    TestHelper.setField( strategy, "xmlDocumentUrlReader", xmlDocumentUrlReader );
    when( xmlDocumentUrlReader.getDocument( anyString() ) ).thenReturn( Optional.empty() );
    when( xmlDocumentUrlReader.getDocument( String.format( INSECURE_AUTOCONF_URL_2, DOMAIN ) ) )
        .thenReturn( Optional.of( TestHelper.readDocumentFromFile( MOCK_SIMPLE ) ) );
    when( txtDnsResolver.getTxtRecords( DOMAIN ) ).thenReturn( List.of() );

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


  private static MozillaAutoconfMailserverConfigurationDiscoveryStrategy createStrategy() {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF ).build();
    return new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context );
  }


  private static MozillaAutoconfMailserverService findOne( List<MailserverService> configs, Protocol protocol ) {

    return findAll( configs, protocol ).stream().findFirst().orElseThrow();
  }


  private static List<MozillaAutoconfMailserverService> findAll( List<MailserverService> configs, Protocol protocol ) {

    return configs.stream().filter( c -> c.getProtocol() == protocol ).map( MozillaAutoconfMailserverService.class::cast ).toList();
  }


  private static void assertMozillaDefaultSmtp( MozillaAutoconfMailserverService smtp ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONF, smtp.getConfigurationMethod() );
    assertEquals( Protocol.SMTP, smtp.getProtocol() );
    assertEquals( "smtp.googlemail.com", smtp.getHost() );
    assertEquals( 587, smtp.getPort() );
    assertEquals( "%EMAILLOCALPART%", smtp.getUsername() );
    assertEquals( "optional: the user's password", smtp.getPassword() );
    assertEquals( SocketType.STARTTLS, smtp.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), smtp.getAuthentications() );
    assertTrue( smtp.getOAuth2s().isEmpty() );
  }


  private static void assertSimpleSmtp( MozillaAutoconfMailserverService smtp ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONF, smtp.getConfigurationMethod() );
    assertEquals( Protocol.SMTP, smtp.getProtocol() );
    assertEquals( "smtp.example.com", smtp.getHost() );
    assertEquals( 465, smtp.getPort() );
    assertNull( smtp.getUsername() );
    assertNull( smtp.getPassword() );
    assertEquals( SocketType.SSL, smtp.getSocketType() );
    assertEquals( Set.of( Authentication.CLIENT_IP_ADDRESS ), smtp.getAuthentications() );
    assertTrue( smtp.getOAuth2s().isEmpty() );
  }


  private static void assertOAuth2Smtp( MozillaAutoconfMailserverService smtp ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONF, smtp.getConfigurationMethod() );
    assertEquals( Protocol.SMTP, smtp.getProtocol() );
    assertEquals( "smtp.example.com", smtp.getHost() );
    assertEquals( 465, smtp.getPort() );
    assertEquals( "%EMAILADDRESS%", smtp.getUsername() );
    assertNull( smtp.getPassword() );
    assertEquals( SocketType.SSL, smtp.getSocketType() );
    assertEquals( Set.of( Authentication.OAUTH2, Authentication.PASSWORD_CLEARTEXT ), smtp.getAuthentications() );
    assertOAuth2Details( smtp );
  }


  private static void assertMozillaDefaultPop3( MozillaAutoconfMailserverService pop3 ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONF, pop3.getConfigurationMethod() );
    assertEquals( Protocol.POP3, pop3.getProtocol() );
    assertEquals( "pop.example.com", pop3.getHost() );
    assertEquals( 995, pop3.getPort() );
    assertEquals( "%EMAILLOCALPART%", pop3.getUsername() );
    assertEquals( "optional: the user's password", pop3.getPassword() );
    assertEquals( SocketType.SSL, pop3.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), pop3.getAuthentications() );
    assertTrue( pop3.getOAuth2s().isEmpty() );
  }


  private static void assertSimpleImap( MozillaAutoconfMailserverService imap ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONF, imap.getConfigurationMethod() );
    assertEquals( Protocol.IMAP, imap.getProtocol() );
    assertEquals( "imap.example.com", imap.getHost() );
    assertEquals( 993, imap.getPort() );
    assertEquals( "%EMAILLOCALPART%", imap.getUsername() );
    assertNull( imap.getPassword() );
    assertEquals( SocketType.SSL, imap.getSocketType() );
    assertEquals( Set.of( Authentication.PASSWORD_CLEARTEXT ), imap.getAuthentications() );
    assertTrue( imap.getOAuth2s().isEmpty() );
  }


  private static void assertOAuth2Imap( MozillaAutoconfMailserverService imap ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONF, imap.getConfigurationMethod() );
    assertEquals( Protocol.IMAP, imap.getProtocol() );
    assertEquals( "imap.example.com", imap.getHost() );
    assertEquals( 993, imap.getPort() );
    assertEquals( "%EMAILADDRESS%", imap.getUsername() );
    assertNull( imap.getPassword() );
    assertEquals( SocketType.SSL, imap.getSocketType() );
    assertEquals( Set.of( Authentication.OAUTH2, Authentication.PASSWORD_CLEARTEXT ), imap.getAuthentications() );
    assertOAuth2Details( imap );
  }


  private static void assertOAuth2Details( MozillaAutoconfMailserverService service ) {

    assertEquals( 1, service.getOAuth2s().size() );
    var oAuth2 = service.getOAuth2s().iterator().next();
    assertEquals( "login.yahoo.com", oAuth2.getIssuer() );
    assertEquals( "mail-w", oAuth2.getScope() );
    assertEquals( "https://api.login.yahoo.com/oauth2/request_auth", oAuth2.getAuthUrl() );
    assertEquals( "https://api.login.yahoo.com/oauth2/get_token", oAuth2.getTokenUrl() );
  }
}
