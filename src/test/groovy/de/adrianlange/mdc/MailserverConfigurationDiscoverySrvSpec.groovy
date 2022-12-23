package de.adrianlange.mdc

import de.adrianlange.mcd.MailserverConfigurationDiscovery
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder
import de.adrianlange.mcd.model.ConfigurationMethod
import de.adrianlange.mcd.model.Protocol
import de.adrianlange.mcd.model.SocketType
import de.adrianlange.mcd.model.SrvRecordMailserverService
import spock.lang.Specification


class MailserverConfigurationDiscoverySrvSpec extends Specification {

    def "test lookup all SRV records"() {

        given:
        def domain = "adrianlange.de"
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .build()

        when:
        def configs = MailserverConfigurationDiscovery.discover(domain, context)

        then:
        configs.size() == 3

        when:
        def smtp = configs.findAll { it.protocol == Protocol.SMTP }.first() as SrvRecordMailserverService
        def imap = configs.findAll { it.protocol == Protocol.IMAP }.first() as SrvRecordMailserverService
        def pop3 = configs.findAll { it.protocol == Protocol.POP3 }.first() as SrvRecordMailserverService

        then:
        matchConfig(smtp, Protocol.SMTP, "smtp.mailbox.org", 465, null, 0, 1)
        matchConfig(imap, Protocol.IMAP, "imap.mailbox.org", 993, SocketType.SSL, 0, 1)
        matchConfig(pop3, Protocol.POP3, "pop3.mailbox.org", 995, SocketType.SSL, 10, 1)
    }

    private static boolean matchConfig(SrvRecordMailserverService service, Protocol protocol, String host, int port, SocketType socketType, int priority, int weight) {
        return service.configurationMethod == ConfigurationMethod.RFC_61186 && service.protocol == protocol
                && service.host == host && service.port == port && service.socketType == socketType
                && service.priority == priority && service.weight == weight
    }
}
