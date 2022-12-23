package de.adrianlange.mdc.strategy.srvrecord

import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext
import de.adrianlange.mcd.MailserverConfigurationDiscoveryContextBuilder
import de.adrianlange.mcd.infrastructure.dns.SrvDnsResolver
import de.adrianlange.mcd.model.ConfigurationMethod
import de.adrianlange.mcd.model.Protocol
import de.adrianlange.mcd.model.SrvRecordMailserverService
import de.adrianlange.mcd.strategy.EmailAddress
import de.adrianlange.mcd.strategy.srvrecord.SrvRecordMailserverConfigurationDiscoveryStrategy
import org.xbill.DNS.DClass
import org.xbill.DNS.Name
import org.xbill.DNS.SRVRecord
import spock.lang.Specification

class SrvRecordMailserverConfigurationDiscoveryStrategySpec extends Specification {

    def "test no SRV records exist"() {

        given:
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .build()
        def srvDnsResolver = Mock(SrvDnsResolver)
        def strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy(context)
        strategy.srvDnsResolver = srvDnsResolver

        when:
        def configs = strategy.getMailserverServices(EmailAddress.DomainPart.of("example.com"))

        then:
        1 * srvDnsResolver.getSrvRecords("example.com", "_submission") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_imap") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_imaps") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3s") >> []
        0 * _
        and:
        configs.isEmpty()
    }

    def "test discover SMTP over SRV records"() {

        given:
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .withDiscoveryScopes(MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION)
                .build()
        def srvDnsResolver = Mock(SrvDnsResolver)
        def strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy(context)
        strategy.srvDnsResolver = srvDnsResolver

        when:
        def configs = strategy.getMailserverServices(EmailAddress.DomainPart.of("example.com"))

        then:
        1 * srvDnsResolver.getSrvRecords("example.com", "_submission") >> [
                new SRVRecord(Name.fromString("example.com."), DClass.IN, 3600, 0, 1, 465, Name.fromString("smtp.example.com."))
        ]
        0 * _
        and:
        configs.size() == 1
        def config = configs[0] as SrvRecordMailserverService
        config.configurationMethod == ConfigurationMethod.RFC_61186
        config.host == "smtp.example.com"
        config.port == 465
        config.socketType == null
        config.protocol == Protocol.SMTP
        config.priority == 0
        config.weight == 1
    }


    def "test discover only submission SRV records"() {

        given:
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .withDiscoveryScopes(MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION)
                .build()
        def srvDnsResolver = Mock(SrvDnsResolver)
        def strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy(context)
        strategy.srvDnsResolver = srvDnsResolver

        when:
        def configs = strategy.getMailserverServices(EmailAddress.DomainPart.of("example.com"))

        then:
        1 * srvDnsResolver.getSrvRecords("example.com", "_submission") >> []
        0 * _
        and:
        configs.isEmpty()
    }


    def "test discover only reception SRV records"() {

        given:
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .withDiscoveryScopes(MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION)
                .build()
        def srvDnsResolver = Mock(SrvDnsResolver)
        def strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy(context)
        strategy.srvDnsResolver = srvDnsResolver

        when:
        def configs = strategy.getMailserverServices(EmailAddress.DomainPart.of("example.com"))

        then:
        1 * srvDnsResolver.getSrvRecords("example.com", "_imap") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_imaps") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3s") >> []
        0 * _
        and:
        configs.isEmpty()
    }


    def "test discover multiple SRV records of same type"() {

        given:
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .withDiscoveryScopes(MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION)
                .build()
        def srvDnsResolver = Mock(SrvDnsResolver)
        def strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy(context)
        strategy.srvDnsResolver = srvDnsResolver

        when:
        def configs = strategy.getMailserverServices(EmailAddress.DomainPart.of("example.com"))

        then:
        1 * srvDnsResolver.getSrvRecords("example.com", "_imap") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_imaps") >> [
                new SRVRecord(Name.fromString("example.com."), DClass.IN, 300, 0, 1, 993, Name.fromString("imap.example.com.")),
                new SRVRecord(Name.fromString("example.com."), DClass.IN, 300, 10, 1, 19993, Name.fromString("imap2.example.com."))
        ]
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3s") >> []
        0 * _
        and:
        configs.size() == 2
        configs.findAll { it.port == 993 }.size() == 1
        configs.findAll { it.port == 19993 }.size() == 1
    }


    def "test ignore empty targets"() {

        given:
        def context = new MailserverConfigurationDiscoveryContextBuilder()
                .withConfigurationMethods(ConfigurationMethod.RFC_61186)
                .withDiscoveryScopes(MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION)
                .build()
        def srvDnsResolver = Mock(SrvDnsResolver)
        def strategy = new SrvRecordMailserverConfigurationDiscoveryStrategy(context)
        strategy.srvDnsResolver = srvDnsResolver

        when:
        def configs = strategy.getMailserverServices(EmailAddress.DomainPart.of("example.com"))

        then:
        1 * srvDnsResolver.getSrvRecords("example.com", "_imap") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_imaps") >> []
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3") >> [
                new SRVRecord(Name.fromString("example.com."), DClass.IN, 300, 0, 1, 110, Name.fromString("."))]
        1 * srvDnsResolver.getSrvRecords("example.com", "_pop3s") >> []
        0 * _
        and:
        configs.size() == 0
    }
}
