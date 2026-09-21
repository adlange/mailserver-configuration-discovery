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
package de.adrianlange.mcd;

import de.adrianlange.mcd.model.Authentication;
import de.adrianlange.mcd.model.ConfigurationMethod;
import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.MozillaAutoconfigMailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.SrvRecordMailserverService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MailserverConfigurationDiscoveryTest {

  @Test
  void testLookupAllSrvRecords() {

    var domain = "adrianlange.de";
    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.RFC_6186 ).build();

    var configs = MailserverConfigurationDiscovery.discover( domain, context );

    assertEquals( 3, configs.size() );

    var smtp = findOneSrv( configs, Protocol.SMTP );
    var imap = findOneSrv( configs, Protocol.IMAP );
    var pop3 = findOneSrv( configs, Protocol.POP3 );

    assertSrvConfig( smtp, Protocol.SMTP, "smtp.mailbox.org", 465, null, 0, 1 );
    assertSrvConfig( imap, Protocol.IMAP, "imap.mailbox.org", 993, SocketType.SSL, 0, 1 );
    assertSrvConfig( pop3, Protocol.POP3, "pop3.mailbox.org", 995, SocketType.SSL, 10, 1 );
  }


  @Test
  void testLookupAllMozillaAutoconfigRecords() {

    var domain = "adrianlange.de";
    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONFIG )
        // autoconfig.adrianlange.de points to the hosting provider's autoconfig server, which is reachable via
        // plain HTTP only (it redirects to HTTPS on its own domain, its certificate does not cover this subdomain)
        .withInsecureHttpAllowed( true ).build();

    var configs = MailserverConfigurationDiscovery.discover( domain, context );

    assertEquals( 6, configs.size() );
    assertMozillaAutoconfigServices( configs, "%EMAILADDRESS%" );
  }


  @Test
  void testLookupAllMozillaAutoconfigRecordsForEmailAddress() {

    var email = "dummy@adrianlange.de";
    var context =
        new MailserverConfigurationDiscoveryContextBuilder().withConfigurationMethods( ConfigurationMethod.MOZILLA_AUTOCONFIG )
        // autoconfig.adrianlange.de points to the hosting provider's autoconfig server, which is reachable via
        // plain HTTP only (it redirects to HTTPS on its own domain, its certificate does not cover this subdomain)
        .withInsecureHttpAllowed( true ).build();

    var configs = MailserverConfigurationDiscovery.discover( EmailAddress.of( email ), context );

    assertEquals( 6, configs.size() );
    assertMozillaAutoconfigServices( configs, email );
  }


  private static void assertMozillaAutoconfigServices( Set<MailserverService> configs, String expectedUsername ) {

    var smtps = findAllMozilla( configs, Protocol.SMTP );
    var imaps = findAllMozilla( configs, Protocol.IMAP );
    var pop3s = findAllMozilla( configs, Protocol.POP3 );

    assertEquals( 2, smtps.size() );
    assertEquals( 2, imaps.size() );
    assertEquals( 2, pop3s.size() );

    assertMozillaConfig( findBySocketType( smtps, SocketType.SSL ), Protocol.SMTP, "smtp.mailbox.org", 465,
        SocketType.SSL, expectedUsername );
    assertMozillaConfig( findBySocketType( smtps, SocketType.STARTTLS ), Protocol.SMTP, "smtp.mailbox.org", 587,
        SocketType.STARTTLS, expectedUsername );
    assertMozillaConfig( findBySocketType( imaps, SocketType.SSL ), Protocol.IMAP, "imap.mailbox.org", 993,
        SocketType.SSL, expectedUsername );
    assertMozillaConfig( findBySocketType( imaps, SocketType.STARTTLS ), Protocol.IMAP, "imap.mailbox.org", 143,
        SocketType.STARTTLS, expectedUsername );
    assertMozillaConfig( findBySocketType( pop3s, SocketType.SSL ), Protocol.POP3, "pop3.mailbox.org", 995,
        SocketType.SSL, expectedUsername );
    assertMozillaConfig( findBySocketType( pop3s, SocketType.STARTTLS ), Protocol.POP3, "pop3.mailbox.org", 110,
        SocketType.STARTTLS, expectedUsername );
  }


  private static SrvRecordMailserverService findOneSrv( Set<MailserverService> configs, Protocol protocol ) {

    return configs.stream().filter( c -> c.getProtocol() == protocol ).map( SrvRecordMailserverService.class::cast ).findFirst().orElseThrow();
  }


  private static List<MozillaAutoconfigMailserverService> findAllMozilla( Set<MailserverService> configs,
                                                                        Protocol protocol ) {

    return configs.stream().filter( c -> c.getProtocol() == protocol ).map( MozillaAutoconfigMailserverService.class::cast ).toList();
  }


  private static MozillaAutoconfigMailserverService findBySocketType( List<MozillaAutoconfigMailserverService> services,
                                                                    SocketType socketType ) {

    return services.stream().filter( s -> s.getSocketType() == socketType ).findFirst().orElseThrow();
  }


  private static void assertSrvConfig( SrvRecordMailserverService service, Protocol protocol, String host, int port,
                                       SocketType socketType, int priority, int weight ) {

    assertEquals( ConfigurationMethod.RFC_6186, service.getConfigurationMethod() );
    assertEquals( protocol, service.getProtocol() );
    assertEquals( host, service.getHost() );
    assertEquals( port, service.getPort() );
    assertEquals( socketType, service.getSocketType() );
    assertEquals( priority, service.getPriority() );
    assertEquals( weight, service.getWeight() );
  }


  private static void assertMozillaConfig( MozillaAutoconfigMailserverService service, Protocol protocol, String host,
                                           int port, SocketType socketType, String username ) {

    assertEquals( ConfigurationMethod.MOZILLA_AUTOCONFIG, service.getConfigurationMethod() );
    assertEquals( protocol, service.getProtocol() );
    assertEquals( host, service.getHost() );
    assertEquals( port, service.getPort() );
    assertEquals( socketType, service.getSocketType() );
    assertTrue( service.getAuthentications().contains( Authentication.PASSWORD_CLEARTEXT ) );
    assertEquals( username, service.getUsername() );
    assertNull( service.getPassword() );
    assertTrue( service.getOAuth2s().isEmpty() );
  }
}
