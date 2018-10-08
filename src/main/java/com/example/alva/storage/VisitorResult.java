package com.example.alva.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;

public class VisitorResult {

    private static final String SEPARATOR = "|-%-|";
    private static final Splitter SPLITTER = Splitter.on(SEPARATOR);
    @JsonProperty("process_id")
    private String id;
    @JsonIgnore
    private final File tempFile;
    @JsonIgnore
    private Map<String, AtomicInteger> bulk;

    public VisitorResult(final VisitorProcess process) throws IOException {
        this.id = process.getId();
        this.tempFile = Files.createTempFile("alva_", ".json").toFile();
        this.bulk = new ConcurrentHashMap<>();
    }

    private static Map<String, Integer> readAndCompressCollectedData(final Path path) {
        try (final Stream<String> lines = Files.lines(path)) {
            final Map<String, Integer> compressedData = new HashMap<>();
            lines
                .map(VisitorResult::parseLineIfPossible)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(pair -> {
                    final String key = pair.getKey();
                    final Integer existingValue = Optional.ofNullable(compressedData.get(key)).orElse(0);
                    compressedData.put(key, existingValue + pair.getValue());
                });
            return compressedData;
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot read underlying temp file " + path, e);
        }
    }

    private static Optional<Pair<String, Integer>> parseLineIfPossible(final String s) {
        final List<String> split = SPLITTER.splitToList(s);

        if (split.size() != 2) {
            return Optional.empty();
        }
        return Optional.of(Pair.of(split.get(0), Integer.valueOf(split.get(1))));
    }

    public String getId() {
        return this.id;
    }

    @JsonProperty("visited_urls")
    public Map<String, Integer> getFoundURLs() {
        return readAndCompressCollectedData(this.tempFile.toPath());
    }

    public synchronized void increaseCount(final Pair<URI, Integer> taken) {
        final String key = taken.getLeft().toString();
        if (!this.bulk.containsKey(key)) {
            this.bulk.put(key, new AtomicInteger());
        }
        this.bulk.get(key).getAndAdd(taken.getRight());
    }

    public int getNumberOfUniqueURIs() {
        return this.getFoundURLs().size();
    }

    public int getOccurrenceOfURI(final URI uri) {
        final String key = uri.toString();
        if (!this.getFoundURLs().containsKey(key)) {
            return 0;
        }
        return this.getFoundURLs().get(key);
    }

    public synchronized void updateBulk() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.tempFile, true));
            PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            for (final Map.Entry<String, AtomicInteger> entry : this.bulk.entrySet()) {
                printWriter.println(entry.getKey() + SEPARATOR + entry.getValue().get());
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot read underlying temp file " + this.tempFile, e);
        } finally {
            this.bulk.clear();
        }
    }
}
