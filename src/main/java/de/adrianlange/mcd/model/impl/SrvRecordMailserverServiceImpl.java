package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.SrvRecordMailserverService;


public class SrvRecordMailserverServiceImpl extends AbstractMailserverService implements SrvRecordMailserverService {

  private Integer weight;


  public SrvRecordMailserverServiceImpl( ConfigurationMethod configurationMethod, Protocol protocol,
                                         SocketType socketType, String host, Integer port, Integer weight ) {

    super( configurationMethod, protocol, socketType, host, port );
    this.weight = weight;
  }


  @Override
  public Integer getWeight() {

    return weight;
  }


  public void setWeight( Integer weight ) {

    this.weight = weight;
  }


  @Override
  public String toString() {

    return super.toString() + ", weight=" + weight;
  }
}
