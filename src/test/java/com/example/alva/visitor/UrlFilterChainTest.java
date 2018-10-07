package com.example.alva.visitor;

import java.net.URI;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.example.alva.TestExcerpt;
import com.example.alva.analytics.LinkVisitorAnalytics;
import com.example.alva.processors.AsyncContentTypeChecker;
import static org.assertj.core.api.Assertions.assertThat;

public class UrlFilterChainTest {

    @Rule
    public MockitoRule initMocks = MockitoJUnit.rule();
    @Mock
    private AsyncContentTypeChecker checkerMock;

    @Before
    public void setUp() {
        Mockito
            .when(this.checkerMock.deliversHTMLDocument(ArgumentMatchers.any()))
            .thenReturn(CompletableFuture.completedFuture(true));
    }

    @Test
    public void filterChildURLs() {
        final UrlFilterChain urlFilterChain = new UrlFilterChain(TestExcerpt.getBaseUri());
        final Queue<Pair<URI, Integer>> childURLs =
            urlFilterChain.filterChildURLs(new LinkVisitorAnalytics(), TestExcerpt.streamLines());

        final List<String> hyperlinks = TestExcerpt.getOriginalHyperlinks();
        final List<String> internalAnchors = TestExcerpt.getInternalAnchors();
        final List<String> clippedURIs = TestExcerpt.getClippedURIs();

        assertThat(childURLs).hasSize(hyperlinks.size() - internalAnchors.size());

        while (!childURLs.isEmpty()) {
            final String relativeHyperlink = this.reconstructRelativeHyperlink(childURLs.poll().getLeft());
            final boolean isExtractedCorrectly = hyperlinks.contains(relativeHyperlink);
            final boolean isClippedCorrectly = clippedURIs.contains(relativeHyperlink);

            assertThat(isExtractedCorrectly || isClippedCorrectly).isTrue();
        }
    }

    private String reconstructRelativeHyperlink(final URI uri) {
        final StringBuilder builder = new StringBuilder(32);
        builder.append(uri.getRawPath());
        if (uri.getRawFragment() != null) {
            builder.append('#').append(uri.getRawFragment());
        }
        if (uri.getRawQuery() != null) {
            builder.append(uri.getRawQuery());
        }
        return builder.toString();
    }
}
