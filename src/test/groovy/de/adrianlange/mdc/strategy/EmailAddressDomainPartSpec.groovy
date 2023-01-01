package de.adrianlange.mdc.strategy

import de.adrianlange.mcd.strategy.EmailAddress
import spock.lang.Specification

class EmailAddressDomainPartSpec extends Specification {

    def "create DomainPart of domain #domain"() {

        when:
        def domainPart = EmailAddress.DomainPart.of( domain )

        then:
        domainPart.toIdn() == idnDomain
        domainPart.toUnicode() == unicodeDomain
        domainPart.toString() == unicodeDomain

        where:
        domain                              || unicodeDomain        | idnDomain
        "sub.adrianlange.de"                || "sub.adrianlange.de" | "sub.adrianlange.de"
        "ädrianlange.de"                    || "ädrianlange.de"     | "xn--drianlange-p5a.de"
        "xn--sb-xka.xn--adrinlnge-y2a4r.fr" || "süb.adriänlönge.fr" | "xn--sb-xka.xn--adrinlnge-y2a4r.fr"
    }

    def "create DomainPart of unicode domain #unicodeDomain"() {

        when:
        def domainPart = EmailAddress.DomainPart.ofUnicode( unicodeDomain )

        then:
        domainPart.toIdn() == idnDomain
        domainPart.toUnicode() == unicodeDomain
        domainPart.toString() == unicodeDomain

        where:
        unicodeDomain        || idnDomain
        "sub.adrianlange.de" || "sub.adrianlange.de"
        "ädrianlange.de"     || "xn--drianlange-p5a.de"
    }

    def "create DomainPart from unicode null"() {

        when:
        EmailAddress.DomainPart.ofUnicode( null )

        then:
        def e = thrown( IllegalArgumentException )
        e.message == "unicodeDomainPart must not be null!"
    }

    def "create DomainPart of IDN domain #idnDomain"() {

        when:
        def domainPart = EmailAddress.DomainPart.ofIdn( idnDomain )

        then:
        domainPart.toIdn() == idnDomain
        domainPart.toUnicode() == unicodeDomain
        domainPart.toString() == unicodeDomain

        where:
        idnDomain                           || unicodeDomain
        "sub.adrianlange.de"                || "sub.adrianlange.de"
        "xn--sb-xka.xn--adrinlnge-y2a4r.fr" || "süb.adriänlönge.fr"
    }

    def "create DomainPart from IDN null"() {

        when:
        EmailAddress.DomainPart.ofIdn( null )

        then:
        def e = thrown( IllegalArgumentException )
        e.message == "idnDomainPart must not be null!"
    }

    def "test creating valid domain #domain"() {
        when:
        EmailAddress.DomainPart.of( domain )
        then:
        noExceptionThrown()

        where:
        domain << [ "localhost", "adrianlange.de", "sub.ädrianlange.fr", "xn--sb-xka.xn--adrinlnge-y2a4r.fr" ]
    }

    def "test creating invalid domain #domain"() {
        when:
        EmailAddress.DomainPart.of( domain )
        then:
        def e = thrown( IllegalArgumentException )
        e.message == "Domain " + domain + " is not valid!"

        where:
        domain << [ "local host", "foo.invalid", "foo@bar" ]
    }
}
