package de.adrianlange.mdc.util

import de.adrianlange.mcd.model.MailserverService
import de.adrianlange.mcd.util.ConcurrencyUtils
import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.util.concurrent.CompletableFuture

class TestHelper {

    /**
     * Waits for all {@link CompletableFuture} in the result of a
     * {@link de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy} and merges all results into one list.
     *
     * @param strategyResult Result of a {@link de.adrianlange.mcd.strategy.MailserverConfigurationDiscoveryStrategy}
     * @return List of {@link MailserverService}
     */
    static <T extends MailserverService> List<T> getResultList( List<CompletableFuture<List<T>>> strategyResult ) {

        return ConcurrencyUtils.waitForAllAndMerge( [ strategyResult ].stream() ) as List<T>
    }


    /**
     * Returns a {@link Document} from a resource path.
     *
     * @param path Path to file in resource directory, e.g. <code>/foo/bar.xml</code>
     * @return Document
     */
    static Document readDocumentFromFile( String path ) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance()
        DocumentBuilder db = dbf.newDocumentBuilder()
        return db.parse( TestHelper.class.getResource( path ).getFile() )
    }
}
