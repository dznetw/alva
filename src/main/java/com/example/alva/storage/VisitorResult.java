package com.example.alva.storage;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VisitorResult {

    @JsonProperty("process_id")
    private String id;
    @JsonProperty("visited_urls")
    private Map<String, AtomicInteger> foundURLs = new ConcurrentHashMap<>();

    public VisitorResult(final VisitorProcess process) {
        this.id = process.getId();
    }

    public String getId() {
        return this.id;
    }

    public void increaseCount(final Pair<URI, Integer> taken) {
        final String key = taken.getLeft().toString();
        if (!this.foundURLs.containsKey(key)) {
            this.foundURLs.put(key, new AtomicInteger());
        }
        this.foundURLs.get(key).getAndAdd(taken.getRight());
    }

    public int getNumberOfUniqueURIs() {
        return this.foundURLs.size();
    }

    public int getOccurrenceOfURI(final URI uri) {
        final String key = uri.toString();
        if (!this.foundURLs.containsKey(key)) {
            return 0;
        }
        return this.foundURLs.get(key).get();
    }
}
