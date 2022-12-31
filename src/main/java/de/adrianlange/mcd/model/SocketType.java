package de.adrianlange.mcd.model;

public enum SocketType {

  PLAIN, SSL, STARTTLS;


  public static SocketType parse( String socketType ) {
    for( SocketType s : SocketType.values() ) {
      if( s.name().equalsIgnoreCase( socketType.trim() ) )
        return s;
    }
    return null;
  }
}
