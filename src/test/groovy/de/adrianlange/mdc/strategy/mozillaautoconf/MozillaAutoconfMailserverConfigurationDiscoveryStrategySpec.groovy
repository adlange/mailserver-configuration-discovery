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

class MozillaAutoconfMailserverConfigurationDiscoveryStrategySpec extends Specification {

    private static final String DOMAIN = "example.com"

    private static final String AUTOCONF_URL_1A = "http://autoconfig.%s/mail/config-v1.1.xml"
    private static final String AUTOCONF_URL_1B = "http://autoconfig.%s/mail/config-v1.1.xml?emailaddress=%s"
    private static final String AUTOCONF_URL_2 = "http://%s/.well-known/autoconfig/mail/config-v1.1.xml"
    private static final String AUTOCONF_URL_3 = "http://dummy-domain.invalid/autoconfig.xml"

    private static final String MOCK_MOZILLA_EXAMPLE = "/autoconf/mozilla-example.xml"


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
        def configs = TestHelper.getResultList( strategy.getMailserverServicesAsync( EmailAddress.DomainPart.of( DOMAIN ) ) )

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
        def configs = TestHelper.getResultList( strategy.getMailserverServicesAsync( EmailAddress.DomainPart.of( DOMAIN ) ) )

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
        smtp.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
        smtp.protocol == Protocol.SMTP
        smtp.host == "smtp.googlemail.com"
        smtp.port == 587
        smtp.username == "%EMAILLOCALPART%"
        smtp.password == "optional: the user's password"
        smtp.socketType == SocketType.STARTTLS
        smtp.authentications.size() == 1
        smtp.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
        smtp.getOAuth2s().isEmpty()

        and:
        pop3.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
        pop3.protocol == Protocol.POP3
        pop3.host == "pop.example.com"
        pop3.port == 995
        pop3.username == "%EMAILLOCALPART%"
        pop3.password == "optional: the user's password"
        pop3.socketType == SocketType.SSL
        pop3.authentications.size() == 1
        pop3.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
        pop3.getOAuth2s().isEmpty()
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
        def configs = TestHelper.getResultList( strategy.getMailserverServicesAsync( EmailAddress.DomainPart.of( DOMAIN ) ) )

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
        smtp.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
        smtp.protocol == Protocol.SMTP
        smtp.host == "smtp.googlemail.com"
        smtp.port == 587
        smtp.username == "%EMAILLOCALPART%"
        smtp.password == "optional: the user's password"
        smtp.socketType == SocketType.STARTTLS
        smtp.authentications.size() == 1
        smtp.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
        smtp.getOAuth2s().isEmpty()

        and:
        pop3.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
        pop3.protocol == Protocol.POP3
        pop3.host == "pop.example.com"
        pop3.port == 995
        pop3.username == "%EMAILLOCALPART%"
        pop3.password == "optional: the user's password"
        pop3.socketType == SocketType.SSL
        pop3.authentications.size() == 1
        pop3.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
        pop3.getOAuth2s().isEmpty()
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
        def configs = TestHelper.getResultList( strategy.getMailserverServicesAsync( EmailAddress.DomainPart.of( DOMAIN ) ) )

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
        smtp.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
        smtp.protocol == Protocol.SMTP
        smtp.host == "smtp.googlemail.com"
        smtp.port == 587
        smtp.username == "%EMAILLOCALPART%"
        smtp.password == "optional: the user's password"
        smtp.socketType == SocketType.STARTTLS
        smtp.authentications.size() == 1
        smtp.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
        smtp.getOAuth2s().isEmpty()

        and:
        pop3.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF
        pop3.protocol == Protocol.POP3
        pop3.host == "pop.example.com"
        pop3.port == 995
        pop3.username == "%EMAILLOCALPART%"
        pop3.password == "optional: the user's password"
        pop3.socketType == SocketType.SSL
        pop3.authentications.size() == 1
        pop3.authentications.contains( Authentication.PASSWORD_CLEARTEXT )
        pop3.getOAuth2s().isEmpty()
    }
}
