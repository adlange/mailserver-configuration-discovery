package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;


public abstract class AbstractMailserverService implements MailserverService {

  private Protocol protocol;

  private SocketType socketType;

  private String host;

  private Integer port;

  private Integer priority;

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
  public Integer getPriority() {

    return priority;
  }


  public void setPriority( Integer priority ) {

    this.priority = priority;
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

    return "configurationMethod=" + configurationMethod + ", protocol=" + protocol + ", host='" + host + '\'' + ", " +
        "port=" + port + ", socketType=" + socketType + ", priority=" + priority;
  }
}
