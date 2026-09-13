package de.adrianlange.mcd.strategy.srvrecord;

import de.adrianlange.mcd.EmailAddress;
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext;
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder;
import de.adrianlange.mcd.infrastructure.dns.SrvDnsResolver;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.SrvRecordMailserverService;
import de.adrianlange.mcd.util.TestHelper;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


class SrvRecordMailserverConfigurationDiscoveryStrategyTest {

  private static final String DOMAIN = "example.com";


  @Test
  void testNoSrvRecordsExist() {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submission" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submissions" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imap" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imaps" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3s" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertTrue( configs.isEmpty() );
  }


  @Test
  void testDiscoverSubmissionsRecordAsSmtpOverImplicitTls() throws TextParseException {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );
    when( srvDnsResolver.getSrvRecords( DOMAIN, "_submissions" ) ).thenReturn( List.of( new SRVRecord( Name.fromString( "_submissions._tcp." + DOMAIN + "." ), DClass.IN, 3600, 5, 10, 465, Name.fromString( "smtp.example.com." ) ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submission" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submissions" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertEquals( 1, configs.size() );
    var config = (SrvRecordMailserverService) configs.get( 0 );
    assertEquals( Protocol.SMTP, config.getProtocol() );
    assertEquals( SocketType.SSL, config.getSocketType() );
    assertEquals( "smtp.example.com", config.getHost() );
    assertEquals( 465, config.getPort() );
    assertEquals( 5, config.getPriority() );
    assertEquals( 10, config.getWeight() );
  }


  @Test
  void testDiscoverSmtpOverSrvRecords() throws TextParseException {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );
    when( srvDnsResolver.getSrvRecords( DOMAIN, "_submission" ) ).thenReturn( List.of( new SRVRecord( Name.fromString( DOMAIN + "." ), DClass.IN, 3600, 0, 1, 465, Name.fromString( "smtp.example.com." ) ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submission" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submissions" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertEquals( 1, configs.size() );
    var config = (SrvRecordMailserverService) configs.get( 0 );
    assertEquals( ConfigurationMethod.RFC_6186, config.getConfigurationMethod() );
    assertEquals( "smtp.example.com", config.getHost() );
    assertEquals( 465, config.getPort() );
    assertNull( config.getSocketType() );
    assertEquals( Protocol.SMTP, config.getProtocol() );
    assertEquals( 0, config.getPriority() );
    assertEquals( 1, config.getWeight() );
  }


  @Test
  void testDiscoverOnlySubmissionSrvRecords() {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submission" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_submissions" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertTrue( configs.isEmpty() );
  }


  @Test
  void testDiscoverOnlyReceptionSrvRecords() {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imap" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imaps" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3s" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertTrue( configs.isEmpty() );
  }


  @Test
  void testDiscoverMultipleSrvRecordsOfSameType() throws TextParseException {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );
    when( srvDnsResolver.getSrvRecords( DOMAIN, "_imaps" ) ).thenReturn( List.of( new SRVRecord( Name.fromString( DOMAIN + "." ), DClass.IN, 300, 0, 1, 993, Name.fromString( "imap.example.com." ) ), new SRVRecord( Name.fromString( DOMAIN + "." ), DClass.IN, 300, 10, 1, 19993, Name.fromString( "imap2.example.com." ) ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imap" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imaps" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3s" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertEquals( 2, configs.size() );
    assertEquals( 1, configs.stream().filter( c -> ((SrvRecordMailserverService) c).getPort() == 993 ).count() );
    assertEquals( 1, configs.stream().filter( c -> ((SrvRecordMailserverService) c).getPort() == 19993 ).count() );
  }


  @Test
  void testIgnoreEmptyTargets() throws TextParseException {

    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION ).build();
    var srvDnsResolver = mock( SrvDnsResolver.class );
    var strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy( context, srvDnsResolver );
    when( srvDnsResolver.getSrvRecords( DOMAIN, "_pop3" ) ).thenReturn( List.of( new SRVRecord( Name.fromString( DOMAIN + "." ), DClass.IN, 300, 0, 1, 110, Name.fromString( "." ) ) ) );

    var configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) );

    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imap" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_imaps" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3" );
    verify( srvDnsResolver ).getSrvRecords( DOMAIN, "_pop3s" );
    verifyNoMoreInteractions( srvDnsResolver );
    assertEquals( 0, configs.size() );
  }
}
