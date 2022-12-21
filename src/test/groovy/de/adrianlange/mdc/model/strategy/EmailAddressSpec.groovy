package de.adrianlange.mdc.model.strategy

import de.adrianlange.mcd.strategy.EmailAddress
import spock.lang.Specification

class EmailAddressSpec extends Specification {

    def "create EmailAddress from string #emailAddress"() {

        when:
        def email = EmailAddress.of(emailAddress)

        then:
        email.localPart == local
        email.domainPart.toUnicode() == unicodeDomain
        email.domainPart.toIdn() == idnDomain
        email.toUnicode() == email.localPart.toString() + "@" + unicodeDomain
        email.toIdn() == email.localPart.toString() + "@" + idnDomain

        where:
        emailAddress                                || local         | unicodeDomain        | idnDomain
        "foo@bar.de"                                || "foo"         | "bar.de"             | "bar.de"
        "foo+bar@baz.gr"                            || "foo+bar"     | "baz.gr"             | "baz.gr"
        "foo.Bar@xn--sb-xka.xn--adrinlnge-y2a4r.fr" || "foo.Bar"     | "süb.adriänlönge.fr" | "xn--sb-xka.xn--adrinlnge-y2a4r.fr"
        "foo+bar+baz@ädrianlange.de"                || "foo+bar+baz" | "ädrianlange.de"     | "xn--drianlange-p5a.de"
        "foo@[192.168.3.2]"                         || "foo"         | "[192.168.3.2]"      | "[192.168.3.2]"
    }

    def "create valid EmailAddress #emailAddress"() {
        when:
        EmailAddress.of(emailAddress)

        then:
        noExceptionThrown()

        where:
        emailAddress << ["foo@bar", "foo.bar+baz@example.com", "Hans123@invalid", "\"Fo o\"@bar.de", "\"Foo@Baz\"@bar.de", "12334567890+x@example.com"]
    }

    def "create invalid EmailAddress #emailAddress"() {
        when:
        EmailAddress.of(emailAddress)

        then:
        thrown(IllegalArgumentException)

        where:
        emailAddress << ["fo o@bar", "fo@o@bar", "fo\"o@localhost", "foo@bar baz", "foo@127.0.0.1", "foo@[123.6.5]", "foo@[2001:db8:1ff::a0b:dbd0]"]
    }
}
