package de.adrianlange.mdc

import de.adrianlange.mcd.MailserverConfigurationDiscovery
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder
import de.adrianlange.mcd.model.Authentication
import de.adrianlange.mcd.model.ConfigurationMethod
import de.adrianlange.mcd.model.MozillaAutoconfMailserverService
import de.adrianlange.mcd.model.Protocol
import de.adrianlange.mcd.model.SocketType
import de.adrianlange.mcd.model.SrvRecordMailserverService
import de.adrianlange.mcd.strategy.EmailAddress
import spock.lang.Specification

class MailserverConfigurationDiscoverySpec extends Specification {

    def "test lookup all SRV records"() {

        given:
            def domain = "adrianlange.de"
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.RFC_61186 )
                    .build()

        when:
            def configs = MailserverConfigurationDiscovery.discover( domain, context )

        then:
            configs.size() == 3

        when:
            def smtp = configs.findAll {
                it.protocol == Protocol.SMTP
            }.first() as SrvRecordMailserverService
            def imap = configs.findAll {
                it.protocol == Protocol.IMAP
            }.first() as SrvRecordMailserverService
            def pop3 = configs.findAll {
                it.protocol == Protocol.POP3
            }.first() as SrvRecordMailserverService

        then:
            matchSrvConfig( smtp, Protocol.SMTP, "smtp.mailbox.org", 465, null, 0, 1 )
            matchSrvConfig( imap, Protocol.IMAP, "imap.mailbox.org", 993, SocketType.SSL, 0, 1 )
            matchSrvConfig( pop3, Protocol.POP3, "pop3.mailbox.org", 995, SocketType.SSL, 10, 1 )
    }

    private static boolean matchSrvConfig( SrvRecordMailserverService service, Protocol protocol, String host, int port,
                                           SocketType socketType, int priority, int weight ) {

        return service.configurationMethod == ConfigurationMethod.RFC_61186 && service.protocol == protocol
                && service.host == host && service.port == port && service.socketType == socketType
                && service.priority == priority && service.weight == weight
    }


    def "test lookup all Mozilla Autoconf records"() {

        given:
            def domain = "adrianlange.de"
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()

        when:
            def configs = MailserverConfigurationDiscovery.discover( domain, context )

        then:
            configs.size() == 6

        when:
            def smtps = configs.findAll {
                it.protocol == Protocol.SMTP
            } as List<MozillaAutoconfMailserverService>
            def imaps = configs.findAll {
                it.protocol == Protocol.IMAP
            } as List<MozillaAutoconfMailserverService>
            def pop3s = configs.findAll {
                it.protocol == Protocol.POP3
            } as List<MozillaAutoconfMailserverService>

        then:
            smtps.size() == 2
            imaps.size() == 2
            pop3s.size() == 2

        when:
            def smtpSsl = smtps.findAll {
                it.socketType == SocketType.SSL
            }.first()
            def smtpStartTls = smtps.findAll {
                it.socketType == SocketType.STARTTLS
            }.first()
            def imapSsl = imaps.findAll {
                it.socketType == SocketType.SSL
            }.first()
            def imapStartTls = imaps.findAll {
                it.socketType == SocketType.STARTTLS
            }.first()
            def pop3Ssl = pop3s.findAll {
                it.socketType == SocketType.SSL
            }.first()
            def pop3StartTls = pop3s.findAll {
                it.socketType == SocketType.STARTTLS
            }.first()

        then:
            assert matchMozillaConfig( smtpSsl, Protocol.SMTP, "smtp.mailbox.org", 465, SocketType.SSL, Authentication.PASSWORD_CLEARTEXT, "%EMAILADDRESS%" )
            assert matchMozillaConfig( smtpStartTls, Protocol.SMTP, "smtp.mailbox.org", 587, SocketType.STARTTLS, Authentication.PASSWORD_CLEARTEXT, "%EMAILADDRESS%" )
            assert matchMozillaConfig( imapSsl, Protocol.IMAP, "imap.mailbox.org", 993, SocketType.SSL, Authentication.PASSWORD_CLEARTEXT, "%EMAILADDRESS%" )
            assert matchMozillaConfig( imapStartTls, Protocol.IMAP, "imap.mailbox.org", 143, SocketType.STARTTLS, Authentication.PASSWORD_CLEARTEXT, "%EMAILADDRESS%" )
            assert matchMozillaConfig( pop3Ssl, Protocol.POP3, "pop3.mailbox.org", 995, SocketType.SSL, Authentication.PASSWORD_CLEARTEXT, "%EMAILADDRESS%" )
            assert matchMozillaConfig( pop3StartTls, Protocol.POP3, "pop3.mailbox.org", 110, SocketType.STARTTLS, Authentication.PASSWORD_CLEARTEXT, "%EMAILADDRESS%" )
    }


    def "test lookup all Mozilla Autoconf records for email address"() {

        given:
            def email = "dummy@adrianlange.de"
            def context = new MailserverConfigurationDiscoveryContextBuilder()
                    .withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONF )
                    .build()

        when:
            def configs = MailserverConfigurationDiscovery.discover( EmailAddress.of( email ), context )

        then:
            configs.size() == 6

        when:
            def smtps = configs.findAll {
                it.protocol == Protocol.SMTP
            } as List<MozillaAutoconfMailserverService>
            def imaps = configs.findAll {
                it.protocol == Protocol.IMAP
            } as List<MozillaAutoconfMailserverService>
            def pop3s = configs.findAll {
                it.protocol == Protocol.POP3
            } as List<MozillaAutoconfMailserverService>

        then:
            smtps.size() == 2
            imaps.size() == 2
            pop3s.size() == 2

        when:
            def smtpSsl = smtps.findAll {
                it.socketType == SocketType.SSL
            }.first()
            def smtpStartTls = smtps.findAll {
                it.socketType == SocketType.STARTTLS
            }.first()
            def imapSsl = imaps.findAll {
                it.socketType == SocketType.SSL
            }.first()
            def imapStartTls = imaps.findAll {
                it.socketType == SocketType.STARTTLS
            }.first()
            def pop3Ssl = pop3s.findAll {
                it.socketType == SocketType.SSL
            }.first()
            def pop3StartTls = pop3s.findAll {
                it.socketType == SocketType.STARTTLS
            }.first()

        then:
            assert matchMozillaConfig( smtpSsl, Protocol.SMTP, "smtp.mailbox.org", 465, SocketType.SSL, Authentication.PASSWORD_CLEARTEXT, "dummy@adrianlange.de" )
            assert matchMozillaConfig( smtpStartTls, Protocol.SMTP, "smtp.mailbox.org", 587, SocketType.STARTTLS, Authentication.PASSWORD_CLEARTEXT, "dummy@adrianlange.de" )
            assert matchMozillaConfig( imapSsl, Protocol.IMAP, "imap.mailbox.org", 993, SocketType.SSL, Authentication.PASSWORD_CLEARTEXT, "dummy@adrianlange.de" )
            assert matchMozillaConfig( imapStartTls, Protocol.IMAP, "imap.mailbox.org", 143, SocketType.STARTTLS, Authentication.PASSWORD_CLEARTEXT, "dummy@adrianlange.de" )
            assert matchMozillaConfig( pop3Ssl, Protocol.POP3, "pop3.mailbox.org", 995, SocketType.SSL, Authentication.PASSWORD_CLEARTEXT, "dummy@adrianlange.de" )
            assert matchMozillaConfig( pop3StartTls, Protocol.POP3, "pop3.mailbox.org", 110, SocketType.STARTTLS, Authentication.PASSWORD_CLEARTEXT, "dummy@adrianlange.de" )
    }

    private static boolean matchMozillaConfig( MozillaAutoconfMailserverService service, Protocol protocol, String host,
                                               int port, SocketType socketType, Authentication authentication,
                                               String username ) {

        return service.configurationMethod == ConfigurationMethod.MOZILLA_AUTOCONF && service.protocol == protocol
                && service.host == host && service.port == port && service.socketType == socketType
                && service.authentications.contains( authentication ) && service.username == username
                && service.password == null && service.getOAuth2s().isEmpty()
    }
}
