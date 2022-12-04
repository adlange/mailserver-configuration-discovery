package de.adrianlange.mcd.model;

public enum Authentication {

  PASSWORD_CLEARTEXT, PASSWORD_ENCRYPTED, NTLM, GSSAPI, CLIENT_IP_ADDRESS, TLS_CLIENT_CERT, OAUTH2, NONE
}
