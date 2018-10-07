package com.example.alva.processors;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.alva.analytics.HyperlinkIssue;
import com.example.alva.analytics.LinkVisitorAnalytics;
import com.example.alva.storage.VisitorProcess;
import com.example.alva.storage.VisitorResult;

@Service
public class AsyncVisitorExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncVisitorExecutionService.class);
    private final AsyncVisitorProcessor visitorProcessor;
    private final AsyncContentTypeChecker contentTypeChecker;

    @Autowired
    public AsyncVisitorExecutionService(final AsyncVisitorProcessor visitorProcessor,
        final AsyncContentTypeChecker contentTypeChecker) {
        this.visitorProcessor = visitorProcessor;
        this.contentTypeChecker = contentTypeChecker;
    }

    private static void takeRemainingURIs(final VisitorProcess origin, final AtomicInteger recursionLevel) {
        origin.takeURIs(recursionLevel.get());
    }

    @Async
    public void execute(final VisitorProcess visitorProcess, final VisitorResult result, final int maxRecursion) {
        try (final VisitorProcess origin = visitorProcess) {
            final AtomicInteger recursionLevel = new AtomicInteger();

            do {
                final int currentLevel = recursionLevel.get();
                final LinkVisitorAnalytics analytics = new LinkVisitorAnalytics(currentLevel);
                if (currentLevel == 0) {
                    final Queue<URI> collect = this.executeSingleProcess(origin, result, analytics);
                    origin.addFoundURIs(currentLevel, this.filterURIsDeliveringHTML(collect, analytics));
                } else {

                    origin
                        .takeURIs(currentLevel)
                        .parallelStream()
                        .map(VisitorProcess::new)
                        .map(process -> this.executeSingleProcess(process, result, analytics))
                        .forEach(queue -> origin.addFoundURIs(currentLevel,
                            this.filterURIsDeliveringHTML(queue, analytics)));
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Analytics: {}", analytics);
                }
                recursionLevel.incrementAndGet();
            } while (recursionLevel.get() < maxRecursion);

            takeRemainingURIs(origin, recursionLevel);
        }
    }

    private Queue<URI> filterURIsDeliveringHTML(final Queue<URI> queue, final LinkVisitorAnalytics analytics) {
        return queue
            .parallelStream()
            .filter(uri -> this.deliversHTMLContent(uri, analytics))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    private Queue<URI> executeSingleProcess(final VisitorProcess visitorProcess, final VisitorResult result,
        final LinkVisitorAnalytics analytics) {
        return this.executeLinkVisitor(visitorProcess, analytics).parallelStream().map(uriIntegerPair -> {
            result.increaseCount(uriIntegerPair);
            return uriIntegerPair.getLeft();
        }).collect(Collectors.toCollection(LinkedList::new));
    }

    private Queue<Pair<URI, Integer>> executeLinkVisitor(final VisitorProcess visitorProcess,
        final LinkVisitorAnalytics analytics) {
        return this.visitorProcessor.execute(visitorProcess, analytics).join();
    }

    private boolean deliversHTMLContent(final URI uri, final LinkVisitorAnalytics analytics) {
        boolean deliversHTMLDocument = this.contentTypeChecker.deliversHTMLDocument(uri).join();

        if (!deliversHTMLDocument) {
            analytics.reportIgnoredHyperlink(uri.toString(), HyperlinkIssue.URI_DELIVERS_NO_HTML_CONTENT);
        }

        return deliversHTMLDocument;
    }
}
