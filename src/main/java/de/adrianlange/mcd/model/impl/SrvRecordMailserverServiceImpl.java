/*
 * Copyright 2022-2026 Adrian Lange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    super( ConfigurationMethod.RFC_6186, protocol, socketType, host, port );
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
