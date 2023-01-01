package de.adrianlange.mcd.util;

import de.adrianlange.mcd.model.MailserverService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


public class ConcurrencyUtils {

  private ConcurrencyUtils() {
  }


  /**
   * Waits for all given {@link CompletableFuture} and merges them into a List of {@link MailserverService}.
   *
   * @param stream Result of a {@link de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy}
   * @param <T>    An implementation of {@link MailserverService}
   * @return A list of {@link MailserverService}
   */
  public static <T extends MailserverService> List<T> waitForAllAndMerge( Stream<List<CompletableFuture<List<T>>>> stream ) {

    //@formatter:off
    return stream
        .flatMap( List::stream )
        .map( CompletableFuture::join )
        .flatMap( List::stream )
        .toList();
    //@formatter:on
  }
}
