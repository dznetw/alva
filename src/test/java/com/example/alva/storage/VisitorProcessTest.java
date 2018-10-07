package com.example.alva.storage;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.example.alva.TestConstants;
import static org.assertj.core.api.Assertions.assertThat;

public class VisitorProcessTest {

    private static final int RECURSION_LEVEL = 0;
    private VisitorProcess visitorProcess;
    private Queue<URI> newURIs;

    @Before
    public void setUp() {
        this.visitorProcess = new VisitorProcess(TestConstants.DEFAULT_URL);
        this.newURIs = new LinkedList<>();
        this.newURIs.add(TestConstants.REDIRECTING_URI);
        this.newURIs.add(TestConstants.NOT_FOUND_URI);
        this.newURIs.add(TestConstants.GOOGLE_LOGO_URI);
    }

    @Test
    public void addFoundURIs() {
        assertThat(this.visitorProcess.takeURIs(RECURSION_LEVEL)).isEmpty();

        this.visitorProcess.addFoundURIs(RECURSION_LEVEL, this.newURIs);

        assertThat(this.visitorProcess.takeURIs(RECURSION_LEVEL))
            .hasSize(3)
            .containsExactlyInAnyOrder(this.newURIs.toArray(new URI[0]));
    }

    @Test
    public void addFoundURIs_statusDone_refuseToAccept() {
        try (final VisitorProcess process = this.visitorProcess) {
            assertThat(process.takeURIs(RECURSION_LEVEL)).isEmpty();
        }

        this.visitorProcess.addFoundURIs(RECURSION_LEVEL, this.newURIs);

        assertThat(this.visitorProcess.takeURIs(RECURSION_LEVEL)).isEmpty();
    }

    @Test
    public void takeURIs_emptiesInternalQueue() {
        assertThat(this.visitorProcess.takeURIs(RECURSION_LEVEL)).isEmpty();

        this.visitorProcess.addFoundURIs(RECURSION_LEVEL, this.newURIs);

        assertThat(this.visitorProcess.takeURIs(RECURSION_LEVEL))
            .hasSize(3)
            .containsExactlyInAnyOrder(this.newURIs.toArray(new URI[0]));

        assertThat(this.visitorProcess.takeURIs(RECURSION_LEVEL)).isEmpty();
    }

    @Test
    public void close() {
        assertThat(this.visitorProcess.getStatus()).isEqualTo(VisitorProcess.ProcessStatus.ACTIVE);

        try (final VisitorProcess process = this.visitorProcess) {
            // close without action
        }
        assertThat(this.visitorProcess.getStatus()).isEqualTo(VisitorProcess.ProcessStatus.DONE);
    }
}
