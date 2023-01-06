package de.adrianlange.mcd.model.impl;

import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.SrvRecordMailserverService;

import java.util.Objects;


public class SrvRecordMailserverServiceImpl extends AbstractMailserverService implements SrvRecordMailserverService {

  private Integer priority;

  private Integer weight;


  public SrvRecordMailserverServiceImpl( Protocol protocol, SocketType socketType, String host, Integer port,
                                         Integer weight ) {

    super( ConfigurationMethod.RFC_61186, protocol, socketType, host, port );
    this.weight = weight;
  }


  @Override
  public Integer getPriority() {

    return priority;
  }


  public void setPriority( Integer priority ) {

    this.priority = priority;
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

    var sb = new StringBuilder( super.toString() );
    if( priority != null )
      sb.append( ", priority=" ).append( priority );
    if( weight != null )
      sb.append( ", weight=" ).append( weight );
    return sb.toString();
  }


  @Override
  public boolean equals( Object o ) {
    if( this == o )
      return true;
    if( o == null || getClass() != o.getClass() )
      return false;
    if( !super.equals( o ) )
      return false;
    SrvRecordMailserverServiceImpl that = (SrvRecordMailserverServiceImpl) o;
    return Objects.equals( priority, that.priority ) && Objects.equals( weight, that.weight );
  }


  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), priority, weight );
  }
}
