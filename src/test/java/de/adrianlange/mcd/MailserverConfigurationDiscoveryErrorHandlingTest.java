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

import de.adrianlange.mcd.model.MailserverService;
import de.adrianlange.mcd.model.Protocol;
import de.adrianlange.mcd.model.SocketType;
import de.adrianlange.mcd.model.impl.SrvRecordMailserverServiceImpl;
import de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MailserverConfigurationDiscoveryErrorHandlingTest {

  private static final EmailAddress.DomainPart DOMAIN = EmailAddress.DomainPart.of( "example.com" );

  private static final MailserverService SERVICE = new SrvRecordMailserverServiceImpl( Protocol.IMAP, SocketType.SSL,
      "imap.example.com", 993, 1 );


  @Test
  void testFailedFutureIsSkippedAndOtherResultsAreReturned() {

    var failing = strategyReturning( CompletableFuture.failedFuture( new IllegalStateException( "boom" ) ) );
    var working = strategyReturning( CompletableFuture.completedFuture( List.of( SERVICE ) ) );

    var result = MailserverConfigurationDiscovery.discover( List.of( failing, working ),
        s -> s.getMailserverServices( DOMAIN ) );

    assertEquals( Set.of( SERVICE ), result );
  }


  @Test
  void testFutureThrowingLaterIsSkipped() {

    CompletableFuture<List<MailserverService>> throwingLater = CompletableFuture.supplyAsync( () -> {
      throw new NumberFormatException( "For input string: \"nine\"" );
    } );
    var failing = strategyReturning( throwingLater );
    var working = strategyReturning( CompletableFuture.completedFuture( List.of( SERVICE ) ) );

    var result = MailserverConfigurationDiscovery.discover( List.of( failing, working ),
        s -> s.getMailserverServices( DOMAIN ) );

    assertEquals( Set.of( SERVICE ), result );
  }


  @Test
  void testStrategyThrowingOnStartIsSkipped() {

    var throwing = mock( MailserverConfigurationDiscoveryStrategy.class );
    when( throwing.getMailserverServices( any( EmailAddress.DomainPart.class ) ) ).thenThrow( new IllegalStateException( "cannot start" ) );
    var working = strategyReturning( CompletableFuture.completedFuture( List.of( SERVICE ) ) );

    var result = MailserverConfigurationDiscovery.discover( List.of( throwing, working ),
        s -> s.getMailserverServices( DOMAIN ) );

    assertEquals( Set.of( SERVICE ), result );
  }


  @Test
  void testNullResultIsTreatedAsEmpty() {

    var nullResult = strategyReturning( CompletableFuture.completedFuture( null ) );

    var result = MailserverConfigurationDiscovery.discover( List.of( nullResult ),
        s -> s.getMailserverServices( DOMAIN ) );

    assertTrue( result.isEmpty() );
  }


  private static MailserverConfigurationDiscoveryStrategy strategyReturning( CompletableFuture<List<MailserverService>> future ) {

    var strategy = mock( MailserverConfigurationDiscoveryStrategy.class );
    when( strategy.getMailserverServices( any( EmailAddress.DomainPart.class ) ) ).thenReturn( List.of( future ) );
    return strategy;
  }
}
