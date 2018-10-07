package com.example.alva.storage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import javax.servlet.http.HttpServletRequest;

import com.example.alva.analytics.LinkVisitorAnalytics;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VisitorProcess implements AutoCloseable {

    private static final String API_BASE = "/api/v1";
    private static final String UPDATE_URL_FORMAT = "%s" + API_BASE + "/visitors/%s";
    private static final String RESULT_URL_FORMAT = "%s" + API_BASE + "/visitors/%s/result";
    @JsonProperty("process_id")
    private String id;
    @JsonIgnore
    private final Map<Integer, BlockingQueue<URI>> workingStack = new ConcurrentHashMap<>();
    @JsonIgnore
    private final Map<Integer, LinkVisitorAnalytics> analytics = new ConcurrentHashMap<>();
    @JsonProperty("process_status")
    private ProcessStatus status = ProcessStatus.ACTIVE;
    @JsonProperty("update_link")
    private String updateLink;
    @JsonProperty("result_link")
    private String resultLink;
    @JsonProperty("base_uri")
    private URI baseURI;
    @JsonIgnore
    private URL baseURL;

    public VisitorProcess(final HttpServletRequest request, final URL baseURL) {
        this.id = UUID.randomUUID().toString();
        this.baseURI = URI.create(baseURL.toString());
        this.baseURL = baseURL;
        this.updateLink = createUpdateLink(request, this.id);
        this.resultLink = createResultLink(request, this.id);
    }

    public VisitorProcess(final URL baseURL) {
        this(null, baseURL);
    }

    public VisitorProcess(final URI uri) {
        this(convertURI(uri));
    }

    private static URL convertURI(final URI uri) {
        try {
            return uri.toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Cannot transform URI " + uri, e);
        }
    }

    private static String createUpdateLink(final HttpServletRequest request, final String id) {
        return String.format(UPDATE_URL_FORMAT, getURLBase(request).orElse("localhost:8080"), id);
    }

    private static String createResultLink(final HttpServletRequest request, final String id) {
        return String.format(RESULT_URL_FORMAT, getURLBase(request).orElse("localhost:8080"), id);
    }

    private static Optional<String> getURLBase(final HttpServletRequest request) {
        try {
            if (request == null) {
                return Optional.empty();
            }
            URL url = new URL(request.getRequestURL().toString());

            String port = url.getPort() == -1 ? "" : ":" + url.getPort();
            return Optional.of(url.getProtocol() + "://" + url.getHost() + port);
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    public String getId() {
        return this.id;
    }

    public URI getBaseURI() {
        return this.baseURI;
    }

    public URL getBaseURL() {
        return this.baseURL;
    }

    public ProcessStatus getStatus() {
        return this.status;
    }

    public String getUpdateLink() {
        return this.updateLink;
    }

    public String getResultLink() {
        return this.resultLink;
    }

    public LinkVisitorAnalytics getAnalyticsForLevel(final int recursionLevel) {
        if (!this.analytics.containsKey(recursionLevel)) {
            this.analytics.put(recursionLevel, new LinkVisitorAnalytics(recursionLevel));
        }
        return this.analytics.get(recursionLevel);
    }

    public void addFoundURIs(final int recursionLevel, final Queue<URI> uris) {
        if (this.status != ProcessStatus.ACTIVE) {
            return;
        }

        if (!this.workingStack.containsKey(recursionLevel)) {
            this.workingStack.put(recursionLevel, new LinkedBlockingQueue<>());
        }
        this.workingStack.get(recursionLevel).addAll(uris);
    }

    public BlockingQueue<URI> takeURIs(final int recursionLevel) {
        if (!this.workingStack.containsKey(recursionLevel)) {
            return new LinkedBlockingQueue<>();
        }
        final BlockingQueue<URI> queue = this.workingStack.get(recursionLevel);
        final BlockingQueue<URI> newQueue = new LinkedBlockingQueue<>(queue);

        queue.clear();
        return newQueue;
    }

    @Override
    public void close() {
        this.status = ProcessStatus.DONE;
    }

    public enum ProcessStatus {
        ACTIVE, DONE
    }
}
