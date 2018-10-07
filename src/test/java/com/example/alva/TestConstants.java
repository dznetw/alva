package com.example.alva;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class TestConstants {

    public static final URI REDIRECTING_URI = URI.create("https://en.wikipedia.org/wiki/Europe_");
    public static final URI NOT_FOUND_URI = URI.create("https://en.wikipedia.org/wiki/Eurpe");
    public static final URI GOOGLE_LOGO_URI =
        URI.create("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png");
    private static final String DEFAULT_URI = "https://en.wikipedia.org/wiki/Europe";
    public static final URL DEFAULT_URL = transformURL(DEFAULT_URI);

    private TestConstants() {}

    private static URL transformURL(final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not transform default test URL '" + url + "'", e);
        }
    }

    public static int getExpectedChildURLs() {
        return 2695;
    }
}
