package de.adrianlange.mcd.infrastructure.dns;

import de.adrianlange.mcd.util.StubResolver;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SrvDnsResolverImplTest {

  @Test
  void testQueriesSrvRecordsUnderTheProtocolLabel() throws TextParseException {

    var record = new SRVRecord( Name.fromString( "_imaps._tcp.srv-test.example.com." ), DClass.IN, 300, 0, 1, 993,
        Name.fromString( "imap.srv-test.example.com." ) );
    var stubResolver = new StubResolver( record );
    var resolver = new SrvDnsResolverImpl( stubResolver );

    var records = resolver.getSrvRecords( "srv-test.example.com", "_imaps" );

    var question = stubResolver.getQueries().getFirst().getQuestion();
    assertEquals( Type.SRV, question.getType() );
    assertEquals( "_imaps._tcp.srv-test.example.com.", question.getName().toString() );
    assertEquals( 1, records.size() );
    var srv = records.iterator().next();
    assertEquals( 993, srv.getPort() );
    assertEquals( "imap.srv-test.example.com.", srv.getTarget().toString() );
  }


  @Test
  void testReturnsEmptyCollectionIfNoSrvRecordExists() {

    var resolver = new SrvDnsResolverImpl( new StubResolver() );

    assertTrue( resolver.getSrvRecords( "srv-none.example.com", "_pop3" ).isEmpty() );
  }
}
