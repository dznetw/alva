package com.example.alva.processors;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.alva.analytics.LinkVisitorAnalytics;
import com.example.alva.storage.VisitorProcess;
import com.example.alva.visitor.LinkVisitor;

@Service
public class AsyncVisitorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncVisitorProcessor.class);

    @Async
    public CompletableFuture<Queue<Pair<URI, Integer>>> execute(final VisitorProcess visitorProcess,
        final LinkVisitorAnalytics analytics) {
        try {
            return CompletableFuture.completedFuture(new LinkVisitor(visitorProcess).visit(analytics));
        } catch (final IOException e) {
            LOGGER.error("Could not open URL properly. Skipping Link.", e);
            return CompletableFuture.completedFuture(new LinkedList<>());
        }
    }
}
