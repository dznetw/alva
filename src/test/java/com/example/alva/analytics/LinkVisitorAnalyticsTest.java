package com.example.alva.analytics;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkVisitorAnalyticsTest {

    private static final String URL_1 = "URL_1";
    private LinkVisitorAnalytics linkVisitorAnalytics;

    @Before
    public void setUp() {
        this.linkVisitorAnalytics = new LinkVisitorAnalytics();
    }

    @Test
    public void reportIgnoredAnchor() {
        for (final AnchorIssue issue : AnchorIssue.values()) {
            this.linkVisitorAnalytics.reportIgnoredAnchor(URL_1, issue);
        }

        assertThat(this.linkVisitorAnalytics.retrieveIssuesForURL(URL_1)).containsExactlyInAnyOrder(
            AnchorIssue.values());
    }

    @Test
    public void reportIgnoredHyperlink() {
        for (final HyperlinkIssue issue : HyperlinkIssue.values()) {
            this.linkVisitorAnalytics.reportIgnoredHyperlink(URL_1, issue);
        }

        assertThat(this.linkVisitorAnalytics.retrieveIssuesForURL(URL_1)).containsExactlyInAnyOrder(
            HyperlinkIssue.values());
    }

    @Test
    public void reportTransformedHyperlink() {
    }
}
