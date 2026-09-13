package de.adrianlange.mdc.strategy;

import de.adrianlange.mcd.strategy.EmailAddress;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class EmailAddressTest {

  @ParameterizedTest
  @CsvSource( delimiter = ';', value = { "foo@bar.de;foo;bar.de;bar.de", "foo+bar@baz.gr;foo+bar;baz.gr;baz.gr", "foo" +
      ".Bar@xn--sb-xka.xn--adrinlnge-y2a4r.fr;foo.Bar;süb.adriänlönge.fr;xn--sb-xka.xn--adrinlnge-y2a4r.fr", "foo+bar" +
      "+baz@ädrianlange.de;foo+bar+baz;ädrianlange.de;xn--drianlange-p5a.de", "foo@[192.168.3.2];foo;[192.168.3.2];" +
      "[192.168.3.2]" } )
  void createEmailAddressFromString( String emailAddress, String local, String unicodeDomain, String idnDomain ) {

    var email = EmailAddress.of( emailAddress );

    assertEquals( local, email.getLocalPart() );
    assertEquals( unicodeDomain, email.getDomainPart().toUnicode() );
    assertEquals( idnDomain, email.getDomainPart().toIdn() );
    assertEquals( email.getLocalPart() + "@" + unicodeDomain, email.toUnicode() );
    assertEquals( email.getLocalPart() + "@" + idnDomain, email.toIdn() );
  }


  @ParameterizedTest
  @ValueSource( strings = { "foo@bar", "foo.bar+baz@example.com", "Hans123@invalid", "\"Fo o\"@bar.de", "\"Foo@Baz" +
      "\"@bar.de", "12334567890+x@example.com" } )
  void createValidEmailAddress( String emailAddress ) {

    assertDoesNotThrow( () -> EmailAddress.of( emailAddress ) );
  }


  @ParameterizedTest
  @ValueSource( strings = { "fo o@bar", "fo@o@bar", "fo\"o@localhost", "foo@bar baz", "foo@127.0.0.1", "foo@[123.6" +
      ".5]", "foo@[2001:db8:1ff::a0b:dbd0]" } )
  void createInvalidEmailAddress( String emailAddress ) {

    assertThrows( IllegalArgumentException.class, () -> EmailAddress.of( emailAddress ) );
  }
}
