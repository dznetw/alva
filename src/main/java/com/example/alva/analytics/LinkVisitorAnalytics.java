package com.example.alva.analytics;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.MoreObjects;

public class LinkVisitorAnalytics {

    private final int recursionLevel;
    private Map<AnchorIssue, Queue<String>> anchorIssues;
    private Map<HyperlinkIssue, Queue<String>> transformationIssues;
    private Map<TransformationSuccess, Queue<Pair<String, String>>> transformationSuccesses;

    public LinkVisitorAnalytics() {
        this(0);
    }

    public LinkVisitorAnalytics(final int recursionLevel) {
        this.recursionLevel = recursionLevel;
        this.anchorIssues = new ConcurrentHashMap<>();
        this.transformationIssues = new ConcurrentHashMap<>();
        this.transformationSuccesses = new ConcurrentHashMap<>();
        initializeIssueMap(this.anchorIssues, AnchorIssue.values());
        initializeIssueMap(this.transformationIssues, HyperlinkIssue.values());
        initializeIssueMap(this.transformationSuccesses, TransformationSuccess.values());
    }

    private static <K extends Enum<K>, V> void initializeIssueMap(final Map<K, Queue<V>> enumMap, final K[] values) {
        for (final K value : values) {
            enumMap.put(value, new LinkedBlockingQueue<>());
        }
    }

    public void reportIgnoredAnchor(final String value, final AnchorIssue issue) {
        this.anchorIssues.get(issue).add(value);
    }

    public void reportIgnoredHyperlink(final String value, final HyperlinkIssue issue) {
        this.transformationIssues.get(issue).add(value);
    }

    public void reportTransformedHyperlink(final String previousValue, final String transformedValue,
        final TransformationSuccess success) {
        this.transformationSuccesses.get(success).add(Pair.of(previousValue, transformedValue));
    }

    public int getRecursionLevel() {
        return this.recursionLevel;
    }

    public Set<Issue> retrieveIssuesForURL(final String value) {
        final Set<Issue> issues = new HashSet<>();
        this.addMatchingIssuesForValue(this.anchorIssues, value, issues);
        this.addMatchingIssuesForValue(this.transformationIssues, value, issues);

        return issues;
    }

    private <T extends Issue> void addMatchingIssuesForValue(final Map<T, Queue<String>> anchorIssues,
        final String value, final Set<Issue> issues) {
        for (final Map.Entry<T, Queue<String>> entry : anchorIssues.entrySet()) {
            if (entry.getValue().contains(value)) {
                issues.add(entry.getKey());
            }
        }
    }

    private static <K extends Enum<K>, V> String flatMap(final Map<K, Queue<V>> map) {
        final StringBuilder builder = new StringBuilder(128);
        builder.append('{');
        for (final Map.Entry<K, Queue<V>> entry : map.entrySet()) {
            builder.append('\n').append(entry.getKey().name()).append(':').append(entry.getValue().size()).append(',');
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.append('}').toString();
    }

    @Override
    public String toString() {
        return MoreObjects
            .toStringHelper(this)
            .add("recursionLevel", this.recursionLevel)
            .add("anchorIssues", flatMap(this.anchorIssues))
            .add("transformationIssues", flatMap(this.transformationIssues))
            .add("transformationSuccesses", flatMap(this.transformationSuccesses))
            .toString();
    }
}
