package de.adrianlange.mcd.strategy.srvrecord;

import de.adrianlange.mcd.MailserverConfigurationDiscoveryContext;
import de.adrianlange.mcd.infrastructure.dns.SrvDnsResolver;
import de.adrianlange.mcd.infrastructure.dns.SrvDnsResolverImpl;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.impl.SrvRecordMailserverServiceImpl;
import de.adrianlange.mcd.strategy.EmailAddress;
import de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy;
import org.xbill.DNS.SRVRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class SrvRecordMailserverConfigurationDiscoveryStrategy implements MailserverConfigurationDiscoveryStrategy {

  private SrvDnsResolver srvDnsResolver;

  private final MailserverConfigurationDiscoveryContext context;


  public SrvRecordMailserverConfigurationDiscoveryStrategy( MailserverConfigurationDiscoveryContext context ) {

    srvDnsResolver = new SrvDnsResolverImpl( context.getDnsLookupContext() );
    this.context = context;
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress emailAddress ) {

    return getMailserverServices( emailAddress.getDomainPart() );
  }


  @Override
  public List<CompletableFuture<List<MailserverService>>> getMailserverServices( EmailAddress.DomainPart domainPart ) {

    //@formatter:off
    return Arrays.stream( SrvProtocol.values() )
        .filter( p -> context.getDiscoveryScopes().contains( p.discoveryScope ) )
        .map( p -> CompletableFuture.supplyAsync( () -> getMailserverServicesForProtocol( domainPart.toIdn(), p ), context.getExecutor() ) )
        .toList();
    //@formatter:on
  }


  private List<MailserverService> getMailserverServicesForProtocol( String idnDomain, SrvProtocol srvProtocol ) {

    List<MailserverService> mailserverServices = new ArrayList<>();
    var srvDnsRecords = srvDnsResolver.getSrvRecords( idnDomain, srvProtocol.protocolPrefix );
    for( SRVRecord srvRecord : srvDnsRecords ) {

      if( srvRecord.getTarget().toString().equals( "." ) )
        break;

      String host = getHostFromName( srvRecord.getTarget() );
      int port = srvRecord.getPort();

      var mailserverService = new SrvRecordMailserverServiceImpl( srvProtocol.protocol, srvProtocol.socketType, host,
          port, srvRecord.getWeight() );
      mailserverService.setPriority( srvRecord.getPriority() );
      mailserverServices.add( mailserverService );
    }
    return mailserverServices;
  }


  private static String getHostFromName( org.xbill.DNS.Name name ) {

    return name.toString().replaceAll( "^\\s*(\\S*[^.\\s]+)[\\s.]*$", "$1" );
  }


  enum SrvProtocol {
    SUBMISSION( "_submission", Protocol.SMTP, null,
        MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION ), IMAP( "_imap", Protocol.IMAP, null,
        MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION ), IMAPS( "_imaps", Protocol.IMAP,
        SocketType.SSL, MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION ), POP3( "_pop3",
        Protocol.POP3, null, MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION ), POP3S( "_pop3s",
        Protocol.POP3, SocketType.SSL, MailserverConfigurationDiscoveryContext.DiscoveryScope.RECEPTION );

    public final String protocolPrefix;

    public final Protocol protocol;

    public final SocketType socketType;

    public final MailserverConfigurationDiscoveryContext.DiscoveryScope discoveryScope;


    SrvProtocol( String protocolPrefix, Protocol protocol, SocketType socketType,
                 MailserverConfigurationDiscoveryContext.DiscoveryScope discoveryScope ) {

      this.protocolPrefix = protocolPrefix;
      this.protocol = protocol;
      this.socketType = socketType;
      this.discoveryScope = discoveryScope;
    }
  }
}
