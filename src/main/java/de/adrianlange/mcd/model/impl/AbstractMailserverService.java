package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;

import java.util.Objects;


public abstract class AbstractMailserverService implements MailserverService {

  private Protocol protocol;

  private SocketType socketType;

  private String host;

  private Integer port;

  private ConfigurationMethod configurationMethod;


  protected AbstractMailserverService( ConfigurationMethod configurationMethod, Protocol protocol,
                                       SocketType socketType, String host, Integer port ) {

    this.configurationMethod = configurationMethod;
    this.protocol = protocol;
    this.socketType = socketType;
    this.host = host;
    this.port = port;
  }


  @Override
  public Protocol getProtocol() {

    return protocol;
  }


  public void setProtocol( Protocol protocol ) {

    this.protocol = protocol;
  }


  @Override
  public SocketType getSocketType() {

    return socketType;
  }


  public void setSocketType( SocketType socketType ) {

    this.socketType = socketType;
  }


  @Override
  public String getHost() {

    return host;
  }


  public void setHost( String host ) {

    this.host = host;
  }


  @Override
  public Integer getPort() {

    return port;
  }


  public void setPort( Integer port ) {

    this.port = port;
  }


  @Override
  public ConfigurationMethod getConfigurationMethod() {

    return configurationMethod;
  }


  public void setConfigurationMethod( ConfigurationMethod configurationMethod ) {

    this.configurationMethod = configurationMethod;
  }


  @Override
  public String toString() {

    var sb = new StringBuilder();
    sb.append( "configurationMethod=" ).append( configurationMethod );
    if( protocol != null )
      sb.append( ", protocol=" ).append( protocol );
    if( host != null )
      sb.append( ", host='" ).append( host ).append( "'" );
    if( port != null )
      sb.append( ", port=" ).append( port );
    if( socketType != null )
      sb.append( ", socketType=" ).append( socketType );
    return sb.toString();
  }


  @Override
  public boolean equals( Object o ) {
    if( this == o )
      return true;
    if( o == null || getClass() != o.getClass() )
      return false;
    AbstractMailserverService that = (AbstractMailserverService) o;
    return protocol == that.protocol && socketType == that.socketType && Objects.equals( host, that.host ) && Objects.equals( port, that.port ) && configurationMethod == that.configurationMethod;
  }


  @Override
  public int hashCode() {
    return Objects.hash( protocol, socketType, host, port, configurationMethod );
  }
}
