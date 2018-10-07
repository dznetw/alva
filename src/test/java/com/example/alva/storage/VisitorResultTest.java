package com.example.alva.storage;

import java.net.URI;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.example.alva.TestConstants;
import static org.assertj.core.api.Assertions.assertThat;

public class VisitorResultTest {

    private static final URI EXISTING_URI = TestConstants.REDIRECTING_URI;
    private static final URI NEW_URI = TestConstants.GOOGLE_LOGO_URI;
    private VisitorResult visitorResult;

    @Before
    public void setUp() {
        this.visitorResult = new VisitorResult(new VisitorProcess(TestConstants.DEFAULT_URL));
        this.visitorResult.increaseCount(Pair.of(EXISTING_URI, 2));
    }

    @Test
    public void increaseCount_newURI() {
        this.visitorResult.increaseCount(Pair.of(NEW_URI, 1));

        assertThat(this.visitorResult.getOccurrenceOfURI(NEW_URI)).isEqualTo(1);
    }

    @Test
    public void increaseCount_existingURI() {
        this.visitorResult.increaseCount(Pair.of(EXISTING_URI, 1));

        assertThat(this.visitorResult.getOccurrenceOfURI(EXISTING_URI)).isEqualTo(3);
    }

    @Test
    public void getOccurrenceOfURI_newURI() {
        assertThat(this.visitorResult.getOccurrenceOfURI(NEW_URI)).isEqualTo(0);
    }

    @Test
    public void getOccurrenceOfURI_existingURI() {
        assertThat(this.visitorResult.getOccurrenceOfURI(EXISTING_URI)).isEqualTo(2);
    }

    @Test
    public void getNumberOfUniqueURIs_withoutModification() {
        assertThat(this.visitorResult.getNumberOfUniqueURIs()).isEqualTo(1);
    }

    @Test
    public void getNumberOfUniqueURIs_afterAddingNewURI() {
        this.visitorResult.increaseCount(Pair.of(NEW_URI, 1));
        assertThat(this.visitorResult.getNumberOfUniqueURIs()).isEqualTo(2);
    }
}
