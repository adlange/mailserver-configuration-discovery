package de.adrianlange.mcd.util;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.TXTRecord;

import java.io.IOException;
import java.util.List;


public class DnsHelper {

  private DnsHelper() {
  }


  public static TXTRecord createTXTRecord( String domain, String value ) {

    try {
      return new TXTRecord( Name.fromString( domain + "." ), DClass.IN, 3600, List.of( "mailconf=", value ) );
    } catch( IOException e ) {
      throw new IllegalStateException( "Could not create TXT record for domain " + domain, e );
    }
  }
}
