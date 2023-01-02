package de.adrianlange.mcd.infrastructure.dns;

import org.xbill.DNS.TXTRecord;

import java.util.Collection;


public interface TxtDnsResolver {

  Collection<TXTRecord> getTxtRecords( String domain );
}
