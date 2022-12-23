package de.adrianlange.mcd.infrastructure.dns;

import org.xbill.DNS.SRVRecord;

import java.util.Collection;


public interface SrvDnsResolver {

  Collection<SRVRecord> getSrvRecords( String domain, String protocolPrefix );
}
