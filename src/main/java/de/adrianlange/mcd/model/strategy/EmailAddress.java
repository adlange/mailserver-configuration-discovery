package de.adrianlange.mcd.model.strategy;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.IDN;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Object representation of an email address.
 *
 * @author Adrian Lange
 */
public class EmailAddress {

  private static final String EMAIL_SEPARATOR = "@";

  private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance( true );

  private final String localPart;

  private final DomainPart domainPart;


  private EmailAddress( String localPart, DomainPart domainPart ) {

    this.localPart = localPart;
    this.domainPart = domainPart;
  }


  /**
   * Parses an email address to an EmailAddress object.
   *
   * @param emailAddress string representation of an email address
   * @return EmailAddress object representation of the given unicode string
   * @throws IllegalArgumentException if given email address is not valid
   */
  public static EmailAddress of( String emailAddress ) {

    var parts = emailAddress.split( EMAIL_SEPARATOR );
    if( parts.length < 2 )
      throw new IllegalArgumentException( String.format( "The given email address %s is invalid!", emailAddress ) );

    var localPartString = String.join( EMAIL_SEPARATOR, Arrays.copyOfRange( parts, 0, parts.length - 1 ) );

    var domainPart = DomainPart.of( parts[parts.length - 1] );

    var emailAddressStr = localPartString + EMAIL_SEPARATOR + domainPart.toIdn();
    if( !EMAIL_VALIDATOR.isValid( emailAddressStr ) )
      throw new IllegalArgumentException( String.format( "The given email address %s is invalid!", emailAddressStr ) );

    return new EmailAddress( localPartString, domainPart );
  }


  /**
   * Parses a local part and a domain part of an email address to an EmailAddress object.
   *
   * @param localPart  String representation of the local part of an email address
   * @param domainPart String representation of the domain part of an email address
   * @return EmailAddress object representation of the given unicode string
   * @throws IllegalArgumentException if given email address is not valid
   */
  public static EmailAddress of( String localPart, String domainPart ) {

    return of( localPart + EMAIL_SEPARATOR + domainPart );
  }


  /**
   * Returns the local part of the email address.
   *
   * @return LocalPart of the email address
   */
  public String getLocalPart() {

    return localPart;
  }


  /**
   * Returns the domain part of the email address.
   *
   * @return DomainPart of the email address
   */
  public DomainPart getDomainPart() {

    return domainPart;
  }


  /**
   * Returns the IDN representation of the email address.
   *
   * @return Email address as IDN string
   */
  public String toIdn() {

    return localPart + EMAIL_SEPARATOR + domainPart.toIdn();
  }


  /**
   * Returns the unicode representation of the email address.
   *
   * @return Email address as unicode string
   */
  public String toUnicode() {

    return localPart + EMAIL_SEPARATOR + domainPart.toUnicode();
  }


  @Override
  public String toString() {

    return toUnicode();
  }


  @Override
  public boolean equals( Object o ) {

    if( this == o )
      return true;
    if( o == null || getClass() != o.getClass() )
      return false;
    EmailAddress that = (EmailAddress) o;
    return localPart.equals( that.localPart ) && domainPart.equals( that.domainPart );
  }


  @Override
  public int hashCode() {

    return Objects.hash( localPart, domainPart );
  }


  /**
   * <p>Internal representation of the domain part of an email address.</p>
   * <p>The domain part of an email <i>john.doe+some-verp@example.com</i> is <i>example.com</i>.</p>
   */
  public static class DomainPart {

    private static final Pattern IPv4_DOMAIN_BOUNDARY = Pattern.compile( "^\\[([0-9.]+)]$" );

    private static final InetAddressValidator INET_ADDRESS_VALIDATOR = InetAddressValidator.getInstance();

    private static final DomainValidator DOMAIN_VALIDATOR = DomainValidator.getInstance( true );

    private final String unicodeDomainPart;


    private DomainPart( String unicodeDomainPart ) {

      if( !isValidIPv4DomainPart( unicodeDomainPart ) && !DOMAIN_VALIDATOR.isValid( unicodeDomainPart ) )
        throw new IllegalArgumentException( String.format( "Domain %s is not valid!", unicodeDomainPart ) );

      this.unicodeDomainPart = unicodeDomainPart;
    }


    private static boolean isValidIPv4DomainPart( String domainPart ) {

      var matcher = IPv4_DOMAIN_BOUNDARY.matcher( domainPart );
      if( !matcher.matches() )
        return false;

      return INET_ADDRESS_VALIDATOR.isValidInet4Address( matcher.group( 1 ) );
    }


    /**
     * Returns the domain part as unicode string.
     *
     * @return Domain part as unicode string
     */
    public String toUnicode() {

      return unicodeDomainPart;
    }


    /**
     * Returns the domain part as IDN string.
     *
     * @return Domain part as IDN string
     */
    public String toIdn() {

      return IDN.toASCII( unicodeDomainPart );
    }


    @Override
    public String toString() {

      return unicodeDomainPart;
    }


    @Override
    public boolean equals( Object o ) {

      if( this == o )
        return true;
      if( o == null || getClass() != o.getClass() )
        return false;
      DomainPart that = (DomainPart) o;
      return unicodeDomainPart.equals( that.unicodeDomainPart );
    }


    @Override
    public int hashCode() {

      return Objects.hash( unicodeDomainPart );
    }


    /**
     * Creates a new domain part. Use {@link #ofUnicode(String)} or {@link #ofIdn(String)} if you know the domain
     * format.
     *
     * @param domainPart String representation of the domain part, must not be null
     * @return new DomainPart object created from the given string representation
     */
    public static DomainPart of( String domainPart ) {

      if( domainPart == null )
        throw new IllegalArgumentException( "domainPart must not be null!" );

      if( domainPart.equals( IDN.toASCII( domainPart ) ) )
        return ofIdn( domainPart );
      return ofUnicode( domainPart );
    }


    /**
     * Creates a new domain part from a unicode domain. IDN domain parts must use {@link #ofIdn(String)}.
     *
     * @param unicodeDomainPart String representation of the domain part, must not be null
     * @return new DomainPart object created from the given string representation
     */
    public static DomainPart ofUnicode( String unicodeDomainPart ) {

      if( unicodeDomainPart == null )
        throw new IllegalArgumentException( "unicodeDomainPart must not be null!" );

      return new DomainPart( unicodeDomainPart );
    }


    /**
     * Creates a new domain part from an IDN domain. Unicode domain parts must use {@link #ofUnicode(String)}.
     *
     * @param idnDomainPart String representation of the domain part, must not be null
     * @return new DomainPart object created from the given string representation
     */
    public static DomainPart ofIdn( String idnDomainPart ) {

      if( idnDomainPart == null )
        throw new IllegalArgumentException( "idnDomainPart must not be null!" );

      return ofUnicode( IDN.toUnicode( idnDomainPart ) );
    }
  }
}
