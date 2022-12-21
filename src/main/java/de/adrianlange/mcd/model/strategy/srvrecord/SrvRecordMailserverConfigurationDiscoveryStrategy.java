package de.adrianlange.mcd.model.strategy.srvrecord;

import de.adrianlange.mcd.infrastructure.dns.SrvDnsResolver;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.context.MailserverConfigurationDiscoveryContext;
import de.adrianlange.mcd.model.impl.SrvRecordMailserverServiceImpl;
import de.adrianlange.mcd.model.strategy.EmailAddress;
import de.adrianlange.mcd.model.strategy.MailserverConfigurationDiscoveryStrategy;
import org.xbill.DNS.SRVRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SrvRecordMailserverConfigurationDiscoveryStrategy implements MailserverConfigurationDiscoveryStrategy {

  private static final ConfigurationMethod CONFIGURATION_METHOD = ConfigurationMethod.RFC_61186;

  private final SrvDnsResolver srvDnsResolver;

  private final MailserverConfigurationDiscoveryContext context;


  public SrvRecordMailserverConfigurationDiscoveryStrategy( MailserverConfigurationDiscoveryContext context ) {

    srvDnsResolver = new SrvDnsResolver( context.getDnsLookupContext() );
    this.context = context;
  }


  @Override
  public List<MailserverService> getMailserverServices( EmailAddress emailAddress ) {

    return getMailserverServices( emailAddress.getDomainPart() );
  }


  @Override
  public List<MailserverService> getMailserverServices( EmailAddress.DomainPart domainPart ) {

    return Arrays.stream( SrvProtocol.values() ).filter( p -> context.getDiscoveryScopes().contains( p.discoveryScope ) ).map( p -> getMailserverServicesForProtocol( domainPart.toIdn(), p ) ).flatMap( List::stream ).toList();
  }


  private List<MailserverService> getMailserverServicesForProtocol( String idnDomain, SrvProtocol srvProtocol ) {
    List<MailserverService> mailserverServices = new ArrayList<>();
    var srvDnsRecords = srvDnsResolver.getSrvRecords( idnDomain, srvProtocol.protocolPrefix );
    for( SRVRecord srvRecord : srvDnsRecords ) {

      if( srvRecord.getTarget().toString().equals( "." ) )
        break;

      String host = getHostFromName( srvRecord.getTarget() );
      int port = srvRecord.getPort();

      var mailserverService = new SrvRecordMailserverServiceImpl( CONFIGURATION_METHOD, srvProtocol.protocol,
          srvProtocol.socketType, host, port, srvRecord.getWeight() );
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
