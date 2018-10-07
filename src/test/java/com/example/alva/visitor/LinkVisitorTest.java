package com.example.alva.visitor;

import java.io.IOException;
import java.net.URI;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.example.alva.TestConstants;
import com.example.alva.analytics.LinkVisitorAnalytics;
import com.example.alva.storage.VisitorProcess;
import static org.assertj.core.api.Assertions.assertThat;

public class LinkVisitorTest {

    @Test
    public void visit() throws IOException {
        final LinkVisitor linkVisitor = new LinkVisitor(new VisitorProcess(TestConstants.DEFAULT_URL));
        final LinkVisitorAnalytics analytics = new LinkVisitorAnalytics();
        final Queue<Pair<URI, Integer>> visit = linkVisitor.visit(analytics);

        assertThat(visit).hasSize(TestConstants.getExpectedChildURLs());
    }
}
