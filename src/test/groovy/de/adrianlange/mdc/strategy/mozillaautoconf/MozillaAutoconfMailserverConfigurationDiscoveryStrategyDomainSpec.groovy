package de.adrianlange.mdc.strategy.mozillaautoconf

import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder
import de.adrianlange.mcd.infrastructure.dns.TxtDnsResolver
import de.adrianlange.mcd.infrastructure.xml.XmlDocumentUrlReader
import de.adrianlange.mcd.model.Authentication
import de.adrianlange.mcd.model.ConfigurationMethod
import de.adrianlange.mcd.model.MozillaAutoconfMailserverService
import de.adrianlange.mcd.model.Protocol
import de.adrianlange.mcd.model.SocketType
import de.adrianlange.mcd.strategy.EmailAddress
import de.adrianlange.mcd.strategy.mozillaautoconf.MozillaAutoconfMailserverConfigurationDiscoveryStrategy
import de.adrianlange.mdc.util.DnsHelper
import de.adrianlange.mdc.util.TestHelper
import spock.lang.Specification

class MozillaAutoconfMailserverConfigurationDiscoveryStrategyDomainSpec extends Specification {

    private static final String DOMAIN = "example.com"

    private static final String AUTOCONF_URL_1A = "http://autoconfig.%s/mail/config-v1.1.xml"
    private static final String AUTOCONF_URL_1B = "http://autoconfig.%s/mail/config-v1.1.xml?emailaddress=%s"
    private static final String AUTOCONF_URL_2 = "http://%s/.well-known/autoconfig/mail/config-v1.1.xml"
    private static final String AUTOCONF_URL_3 = "https://dummy-domain.invalid/autoconfig.xml"

    private static final String MOCK_MOZILLA_EXAMPLE = "/autoconf/mozilla-example.xml"
    private static final String MOCK_SIMPLE = "/autoconf/simple.xml"
    private static final String MOCK_OAUTH2 = "/autoconf/oauth2.xml"


    def "test no autoconf document exist"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.empty()
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.empty()
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ ]
            0 * _
        and:
            configs.isEmpty()
    }

    def "test one autoconf document exist 1A"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) )
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.empty()
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ ]
            0 * _
        and:
            configs.size() == 2

        when:
            def smtp = configs.find {
                it.protocol == Protocol.SMTP
            } as MozillaAutoconfMailserverService
            def pop3 = configs.find {
                it.protocol == Protocol.POP3
            } as MozillaAutoconfMailserverService

        then:
            isMozillaDefaultSmtp( smtp )
            isMozillaDefaultPop3( pop3 )
    }

    def "test one autoconf document exist 2"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.empty()
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) )
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ ]
            0 * _
        and:
            configs.size() == 2

        when:
            def smtp = configs.find {
                it.protocol == Protocol.SMTP
            } as MozillaAutoconfMailserverService
            def pop3 = configs.find {
                it.protocol == Protocol.POP3
            } as MozillaAutoconfMailserverService

        then:
            isMozillaDefaultSmtp( smtp )
            isMozillaDefaultPop3( pop3 )
    }

    def "test one autoconf document exist 3"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.empty()
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.empty()
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_3 ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) )
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ DnsHelper.createTXTRecord( DOMAIN, AUTOCONF_URL_3 ) ]
            0 * _
        and:
            configs.size() == 2

        when:
            def smtp = configs.find {
                it.protocol == Protocol.SMTP
            } as MozillaAutoconfMailserverService
            def pop3 = configs.find {
                it.protocol == Protocol.POP3
            } as MozillaAutoconfMailserverService

        then:
            isMozillaDefaultSmtp( smtp )
            isMozillaDefaultPop3( pop3 )
    }

    def "test multiple config documents won't be merged"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) )
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.empty()
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_3 ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) )
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ DnsHelper.createTXTRecord( DOMAIN, AUTOCONF_URL_3 ) ]
            0 * _
        and:
            configs.size() == 4

        when:
            def smtp = configs.findAll {
                it.protocol == Protocol.SMTP
            } as MozillaAutoconfMailserverService[]
            def pop3 = configs.findAll {
                it.protocol == Protocol.POP3
            } as MozillaAutoconfMailserverService[]

        then:
            smtp.size() == 2
            pop3.size() == 2
        and:
            isMozillaDefaultSmtp( smtp[0] )
            isMozillaDefaultSmtp( smtp[1] )
            isMozillaDefaultPop3( pop3[0] )
            isMozillaDefaultPop3( pop3[1] )
    }

    def "test multiple different config documents are found"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_MOZILLA_EXAMPLE ) )
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_SIMPLE ) )
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_3 ) )
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ ]
            0 * _
        and:
            configs.size() == 4

        when:
            def smtpGoogle = configs.findAll {
                it.protocol == Protocol.SMTP && it.host.contains( "google" )
            } as MozillaAutoconfMailserverService[]
            def smtpExample = configs.findAll {
                it.protocol == Protocol.SMTP && it.host.contains( "example" )
            } as MozillaAutoconfMailserverService[]
            def pop3 = configs.findAll {
                it.protocol == Protocol.POP3
            } as MozillaAutoconfMailserverService[]
            def imap = configs.findAll {
                it.protocol == Protocol.IMAP
            } as MozillaAutoconfMailserverService[]

        then:
            smtpGoogle.size() == 1
            smtpExample.size() == 1
            pop3.size() == 1
            imap.size() == 1
        and:
            isMozillaDefaultSmtp( smtpGoogle[0] )
            isSimpleSmtp( smtpExample[0] )
            isMozillaDefaultPop3( pop3[0] )
            isSimpleImap( imap[0] )
    }

    def "test reading document with OAuth2 information"() {

        given:
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()
            def strategy = new MozillaAutoconfMailserverConfigurationDiscoveryStrategy( context )
            def txtDnsResolver = Mock( TxtDnsResolver )
            strategy.txtDnsResolver = txtDnsResolver
            def xmlDocumentUrlReader = Mock( XmlDocumentUrlReader )
            strategy.xmlDocumentUrlReader = xmlDocumentUrlReader

        when:
            def configs = TestHelper.getResultList( strategy.getMailserverServices( EmailAddress.DomainPart.of( DOMAIN ) ) )

        then:
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1A, DOMAIN ) ) >> Optional.empty()
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_1B, DOMAIN, "" ) )
            1 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_2, DOMAIN ) ) >> Optional.of( TestHelper.readDocumentFromFile( MOCK_OAUTH2 ) )
            0 * xmlDocumentUrlReader.getDocument( String.format( AUTOCONF_URL_3 ) )
            1 * txtDnsResolver.getTxtRecords( DOMAIN ) >> [ ]
            0 * _
        and:
            configs.size() == 2

        when:
            def smtp = configs.find {
                it.protocol == Protocol.SMTP
            } as MozillaAutoconfMailserverService
            def imap = configs.find {
                it.protocol == Protocol.IMAP
            } as MozillaAutoconfMailserverService

        then:
            isOAuth2Smtp( smtp )
            isOAuth2Imap( imap )
    }


    private static boolean isMozillaDefaultSmtp( MozillaAutoconfMailserverService smtp ) {
        return smtp.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
                && smtp.protocol == Protocol.SMTP
                && smtp.host == "smtp.googlemail.com"
                && smtp.port == 587
                && smtp.username == "%EMAILLOCALPART%"
                && smtp.password == "optional: the user's password"
                && smtp.socketType == SocketType.STARTTLS
                && smtp.authentications.size() == 1
                && smtp.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
                && smtp.getOAuth2s().isEmpty()
    }


    private static boolean isSimpleSmtp( MozillaAutoconfMailserverService smtp ) {
        return smtp.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
                && smtp.protocol == Protocol.SMTP
                && smtp.host == "smtp.example.com"
                && smtp.port == 465
                && smtp.username == null
                && smtp.password == null
                && smtp.socketType == SocketType.SSL
                && smtp.authentications.size() == 1
                && smtp.authentications.contains( Authentication.CLIENT_IP_ADDRESS )
                && smtp.getOAuth2s().isEmpty()
    }


    private static boolean isOAuth2Smtp( MozillaAutoconfMailserverService smtp ) {
        return smtp.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
                && smtp.protocol == Protocol.SMTP
                && smtp.host == "smtp.example.com"
                && smtp.port == 465
                && smtp.username == "%EMAILADDRESS%"
                && smtp.password == null
                && smtp.socketType == SocketType.SSL
                && smtp.authentications.size() == 2
                && smtp.authentications.containsAll( Authentication.OAUTH2, Authentication.PASSWORD_CLEARTEXT )
                && smtp.getOAuth2s().size() == 1
                && smtp.getOAuth2s()[0].issuer == "login.yahoo.com"
                && smtp.getOAuth2s()[0].scope == "mail-w"
                && smtp.getOAuth2s()[0].authUrl == "https://api.login.yahoo.com/oauth2/request_auth"
                && smtp.getOAuth2s()[0].tokenUrl == "https://api.login.yahoo.com/oauth2/get_token"
    }


    private static boolean isMozillaDefaultPop3( MozillaAutoconfMailserverService pop3 ) {
        return pop3.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
                && pop3.protocol == Protocol.POP3
                && pop3.host == "pop.example.com"
                && pop3.port == 995
                && pop3.username == "%EMAILLOCALPART%"
                && pop3.password == "optional: the user's password"
                && pop3.socketType == SocketType.SSL
                && pop3.authentications.size() == 1
                && pop3.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
                && pop3.getOAuth2s().isEmpty()
    }


    private static boolean isSimpleImap( MozillaAutoconfMailserverService imap ) {
        return imap.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
                && imap.protocol == Protocol.IMAP
                && imap.host == "imap.example.com"
                && imap.port == 993
                && imap.username == "%EMAILLOCALPART%"
                && imap.password == null
                && imap.socketType == SocketType.SSL
                && imap.authentications.size() == 1
                && imap.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
                && imap.getOAuth2s().isEmpty()
    }


    private static boolean isOAuth2Imap( MozillaAutoconfMailserverService imap ) {
        return imap.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
                && imap.protocol == Protocol.IMAP
                && imap.host == "imap.example.com"
                && imap.port == 993
                && imap.username == "%EMAILADDRESS%"
                && imap.password == null
                && imap.socketType == SocketType.SSL
                && imap.authentications.size() == 2
                && imap.authentications.containsAll( Authentication.OAUTH2, Authentication.PASSWORD_CLEARTEXT )
                && imap.getOAuth2s().size() == 1
                && imap.getOAuth2s()[0].issuer == "login.yahoo.com"
                && imap.getOAuth2s()[0].scope == "mail-w"
                && imap.getOAuth2s()[0].authUrl == "https://api.login.yahoo.com/oauth2/request_auth"
                && imap.getOAuth2s()[0].tokenUrl == "https://api.login.yahoo.com/oauth2/get_token"
    }
}
