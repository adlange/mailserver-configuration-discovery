package de.adrianlange.mcd.util;

import org.xbill.DNS.EDNSOption;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.TSIG;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;


/**
 * dnsjava {@link Resolver} for tests. Answers every query from a fixed list of records: all records whose name and type
 * match the question are returned in the answer section, otherwise an empty NOERROR response (NODATA) is returned. All
 * queries are recorded so tests can assert the queried name and type.
 */
public class StubResolver implements Resolver {

  private final List<Record> records;

  private final List<Message> queries = new ArrayList<>();


  public StubResolver( Record... records ) {

    this.records = List.of( records );
  }


  /**
   * @return All queries received so far, in order
   */
  public List<Message> getQueries() {

    return queries;
  }


  @Override
  public Message send( Message query ) {

    queries.add( query );

    var question = query.getQuestion();
    var response = new Message( query.getHeader().getID() );
    response.getHeader().setFlag( Flags.QR );
    response.getHeader().setFlag( Flags.AA );
    response.getHeader().setRcode( Rcode.NOERROR );
    response.addRecord( question, Section.QUESTION );
    for( Record record : records ) {
      if( record.getName().equals( question.getName() ) && record.getType() == question.getType() )
        response.addRecord( record, Section.ANSWER );
    }
    return response;
  }


  @Override
  public CompletionStage<Message> sendAsync( Message query ) {

    return CompletableFuture.completedFuture( send( query ) );
  }


  @Override
  public CompletionStage<Message> sendAsync( Message query, Executor executor ) {

    return sendAsync( query );
  }


  @Override
  public void setPort( int port ) {
    // ignored
  }


  @Override
  public void setTCP( boolean flag ) {
    // ignored
  }


  @Override
  public void setIgnoreTruncation( boolean flag ) {
    // ignored
  }


  @Override
  public void setEDNS( int version, int payloadSize, int flags, List<EDNSOption> options ) {
    // ignored
  }


  @Override
  public void setTSIGKey( TSIG key ) {
    // ignored
  }


  @Override
  public void setTimeout( Duration timeout ) {
    // ignored
  }


  @Override
  public Duration getTimeout() {

    return Duration.ofSeconds( 1 );
  }
}
