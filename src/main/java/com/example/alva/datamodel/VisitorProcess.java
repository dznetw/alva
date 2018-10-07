package com.example.alva.datamodel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VisitorProcess {

    private static final String API_BASE = "/api/v1";
    private static final String UPDATE_URL_FORMAT = "%s" + API_BASE + "/visitors/%s";
    private static final String RESULT_URL_FORMAT = "%s" + API_BASE + "/visitors/%s/result";
    @JsonProperty("process_id")
    private String id;
    @JsonProperty("base_url")
    private URL baseURL;
    @JsonProperty("process_status")
    private ProcessStatus status = ProcessStatus.ACTIVE;
    @JsonProperty("update_link")
    private String updateLink;
    @JsonProperty("result_link")
    private String resultLink;

    public VisitorProcess(final HttpServletRequest request, final URL baseURL) {
        this.id = UUID.randomUUID().toString();
        this.baseURL = baseURL;
        this.updateLink = createUpdateLink(request, this.id);
        this.resultLink = createResultLink(request, this.id);
    }

    public VisitorProcess(final URL baseURL) {
        this(null, baseURL);
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

    public void setId(final String id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public URL getBaseURL() {
        return this.baseURL;
    }

    public void setBaseURL(final URL baseURL) {
        this.baseURL = baseURL;
    }

    public ProcessStatus getStatus() {
        return this.status;
    }

    public void setStatus(final ProcessStatus status) {
        this.status = status;
    }

    public String getUpdateLink() {
        return this.updateLink;
    }

    public void setUpdateLink(final String updateLink) {
        this.updateLink = updateLink;
    }

    public String getResultLink() {
        return this.resultLink;
    }

    public void setResultLink(final String resultLink) {
        this.resultLink = resultLink;
    }

    public enum ProcessStatus {
        ACTIVE, DONE
    }
}
