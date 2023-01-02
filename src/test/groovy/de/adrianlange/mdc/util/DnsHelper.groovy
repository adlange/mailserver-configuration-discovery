package de.adrianlange.mdc.util

import org.xbill.DNS.DClass
import org.xbill.DNS.Name
import org.xbill.DNS.TXTRecord

class DnsHelper {

    static TXTRecord createTXTRecord( String domain, String value ) {
        return new TXTRecord( Name.fromString( domain + "." ), DClass.IN, 3600, [ "mailconf=", value ] )
    }
}
