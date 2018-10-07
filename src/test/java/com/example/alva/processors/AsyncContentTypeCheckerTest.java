package com.example.alva.processors;

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.example.alva.TestConstants;
import static org.assertj.core.api.Assertions.assertThat;

public class AsyncContentTypeCheckerTest {

    private AsyncContentTypeChecker checker;

    @Before
    public void setUp() {
        this.checker = new AsyncContentTypeChecker();
    }

    @Test
    public void deliversHTMLDocument_defaultURL() throws URISyntaxException {
        final boolean isHTML = this.checker.deliversHTMLDocument(TestConstants.DEFAULT_URL.toURI()).join();

        assertThat(isHTML).isTrue();
    }

    @Test
    public void deliversHTMLDocument_imageURL() {
        final boolean isHTML = this.checker.deliversHTMLDocument(TestConstants.GOOGLE_LOGO_URI).join();

        assertThat(isHTML).isFalse();
    }

    @Test
    public void deliversHTMLDocument_redirectingURL() {
        final boolean isHTML = this.checker.deliversHTMLDocument(TestConstants.REDIRECTING_URI).join();

        assertThat(isHTML).isTrue();
    }

    @Test
    public void deliversHTMLDocument_notFoundURL() {
        final boolean isHTML = this.checker.deliversHTMLDocument(TestConstants.NOT_FOUND_URI).join();

        assertThat(isHTML).isFalse();
    }

    @Test(expected = NullPointerException.class)
    public void deliversHTMLDocument_nullURL() {
        this.checker.deliversHTMLDocument(null);
    }
}
