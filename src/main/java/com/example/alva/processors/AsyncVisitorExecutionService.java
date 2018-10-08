package com.example.alva.processors;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.alva.storage.VisitorProcess;
import com.example.alva.storage.VisitorResult;

@Service
public class AsyncVisitorExecutionService {

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
    public void execute(final VisitorProcess visitorProcess, final VisitorResult result, final int maxRecursion)
        throws InterruptedException {
        try (final VisitorProcess origin = visitorProcess) {
            final AtomicInteger recursionLevel = new AtomicInteger();

            do {
                final int currentLevel = recursionLevel.get();
                if (currentLevel == 0) {
                    final Queue<URI> collect = this.executeSingleProcess(origin, result);

                    if (currentLevel < maxRecursion) {
                        origin.addFoundURIs(currentLevel, this.filterURIsDeliveringHTML(collect));
                    }
                } else {

                    final BlockingQueue<URI> uris = origin.takeURIs(currentLevel - 1);
                    while (!uris.isEmpty()) {
                        final URI taken = uris.take();
                        try (final VisitorProcess process = new VisitorProcess(taken)) {
                            final Queue<URI> queue = this.executeSingleProcess(process, result);

                            if (currentLevel < maxRecursion) {
                                origin.addFoundURIs(currentLevel, this.filterURIsDeliveringHTML(queue));
                            }
                        }
                    }
                }

                recursionLevel.incrementAndGet();
            } while (recursionLevel.get() <= maxRecursion);

            takeRemainingURIs(origin, recursionLevel);
        }
    }

    private Queue<URI> filterURIsDeliveringHTML(final Queue<URI> queue) {
        return queue
            .parallelStream().filter(this::deliversHTMLContent)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    private Queue<URI> executeSingleProcess(final VisitorProcess visitorProcess, final VisitorResult result) {
        final Queue<URI> uris = this.executeLinkVisitor(visitorProcess).parallelStream().map(uriIntegerPair -> {
            result.increaseCount(uriIntegerPair);
            return uriIntegerPair.getLeft();
        }).collect(Collectors.toCollection(LinkedList::new));
        result.updateBulk();
        return uris;
    }

    private Queue<Pair<URI, Integer>> executeLinkVisitor(final VisitorProcess visitorProcess) {
        return this.visitorProcessor.execute(visitorProcess).join();
    }

    private boolean deliversHTMLContent(final URI uri) {
        return this.contentTypeChecker.deliversHTMLDocument(uri).join();
    }
}
