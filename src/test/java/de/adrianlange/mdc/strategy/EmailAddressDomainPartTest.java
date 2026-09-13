package de.adrianlange.mdc.strategy;

import de.adrianlange.mcd.strategy.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class EmailAddressDomainPartTest {

  @ParameterizedTest
  @CsvSource( delimiter = ';', value = { "sub.adrianlange.de;sub.adrianlange.de;sub.adrianlange.de", "ädrianlange.de;" +
      "ädrianlange.de;xn--drianlange-p5a.de", "xn--sb-xka.xn--adrinlnge-y2a4r.fr;süb.adriänlönge.fr;xn--sb-xka" +
      ".xn--adrinlnge-y2a4r.fr" } )
  void createDomainPartOfDomain( String domain, String unicodeDomain, String idnDomain ) {

    var domainPart = EmailAddress.DomainPart.of( domain );

    assertEquals( idnDomain, domainPart.toIdn() );
    assertEquals( unicodeDomain, domainPart.toUnicode() );
    assertEquals( unicodeDomain, domainPart.toString() );
  }


  @ParameterizedTest
  @CsvSource( delimiter = ';', value = { "sub.adrianlange.de;sub.adrianlange.de", "ädrianlange.de;xn--drianlange-p5a" +
      ".de" } )
  void createDomainPartOfUnicodeDomain( String unicodeDomain, String idnDomain ) {

    var domainPart = EmailAddress.DomainPart.ofUnicode( unicodeDomain );

    assertEquals( idnDomain, domainPart.toIdn() );
    assertEquals( unicodeDomain, domainPart.toUnicode() );
    assertEquals( unicodeDomain, domainPart.toString() );
  }


  @Test
  void createDomainPartFromUnicodeNull() {

    var e = assertThrows( IllegalArgumentException.class, () -> EmailAddress.DomainPart.ofUnicode( null ) );
    assertEquals( "unicodeDomainPart must not be null!", e.getMessage() );
  }


  @ParameterizedTest
  @CsvSource( delimiter = ';', value = { "sub.adrianlange.de;sub.adrianlange.de", "xn--sb-xka.xn--adrinlnge-y2a4r.fr;" +
      "süb.adriänlönge.fr" } )
  void createDomainPartOfIdnDomain( String idnDomain, String unicodeDomain ) {

    var domainPart = EmailAddress.DomainPart.ofIdn( idnDomain );

    assertEquals( idnDomain, domainPart.toIdn() );
    assertEquals( unicodeDomain, domainPart.toUnicode() );
    assertEquals( unicodeDomain, domainPart.toString() );
  }


  @Test
  void createDomainPartFromIdnNull() {

    var e = assertThrows( IllegalArgumentException.class, () -> EmailAddress.DomainPart.ofIdn( null ) );
    assertEquals( "idnDomainPart must not be null!", e.getMessage() );
  }


  @ParameterizedTest
  @ValueSource( strings = { "localhost", "adrianlange.de", "sub.ädrianlange.fr", "xn--sb-xka.xn--adrinlnge-y2a4r.fr" } )
  void createValidDomain( String domain ) {

    assertDoesNotThrow( () -> EmailAddress.DomainPart.of( domain ) );
  }


  @ParameterizedTest
  @ValueSource( strings = { "local host", "foo.invalid", "foo@bar" } )
  void createInvalidDomain( String domain ) {

    var e = assertThrows( IllegalArgumentException.class, () -> EmailAddress.DomainPart.of( domain ) );
    assertEquals( "Domain " + domain + " is not valid!", e.getMessage() );
  }
}
