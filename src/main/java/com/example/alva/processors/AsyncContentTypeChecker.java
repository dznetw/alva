package com.example.alva.processors;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

@Service
public class AsyncContentTypeChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncContentTypeChecker.class);
    private static final MediaType[] ALLOWED_TYPES =
        new MediaType[] {MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML};
    private static final Joiner JOINER_FOR_HEADER_VALUES = Joiner.on(",");
    private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";

    private static CompletableFuture<Boolean> makeNegativeResponse() {
        return CompletableFuture.completedFuture(false);
    }

    private static HttpURLConnection sendHEADRequest(final URL url) throws IOException {
        final HttpURLConnection urlConnection = ((HttpURLConnection) url.openConnection());
        urlConnection.setRequestMethod("HEAD");
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.connect();
        return urlConnection;
    }

    private static List<MediaType> parseMediaTypes(final HttpURLConnection urlConnection) {
        final Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        if (!headerFields.containsKey(HEADER_KEY_CONTENT_TYPE)) {
            return Collections.emptyList();
        }

        final List<String> valueList = headerFields.get(HEADER_KEY_CONTENT_TYPE);
        return MediaType.parseMediaTypes(JOINER_FOR_HEADER_VALUES.join(valueList));
    }

    private static boolean returnsHTMLDocument(final List<MediaType> mediaTypes) {
        for (final MediaType mediaType : mediaTypes) {
            if (isCompatibleType(mediaType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCompatibleType(final MediaType mediaType) {
        for (final MediaType allowedType : ALLOWED_TYPES) {
            if (allowedType.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the assigned URL delivers a processable HTML document when connected to.<br/>
     * This implementation follows redirects and uses Spring's
     * {@link org.springframework.http.MediaType#isCompatibleWith(org.springframework.http.MediaType)}
     * for mime type comparisons.<br/>
     * A processable HTML document is identified by its media type which must correspond to one of the following:<ul>
     *     <li>{@link org.springframework.http.MediaType#APPLICATION_XHTML_XML}</li>
     *     <li>{@link org.springframework.http.MediaType#TEXT_HTML}</li>
     * </ul>
     * This method fails fast for unresponsive URLs. {@code false} is guaranteed to be returned immediately when
     * one of the following conditions applies:<ul>
     *     <li>returned status code does not correspond to {@link org.springframework.http.HttpStatus#OK}</li>
     *     <li>"Content-Type" not available</li>
     *     <li>"Content-Type" does not define any media types.</li>
     * </ul>
     * @param uri {@code @NotNull} - to be checked.
     * @return {@code true} if the content served by the assigned URL is a processable HTML document; otherwise
     * false.
     * @see org.springframework.http.MediaType#isCompatibleWith(org.springframework.http.MediaType)
     */
    @Async
    public CompletableFuture<Boolean> deliversHTMLDocument(final URI uri) {
        LOGGER.info("Checking URI: {}", uri);

        try {
            final URL url = Objects.requireNonNull(uri).toURL();
            final HttpURLConnection urlConnection = sendHEADRequest(url);

            if (urlConnection.getResponseCode() != HttpStatus.OK.value()) {
                return makeNegativeResponse();
            }

            final List<MediaType> parsedTypes = parseMediaTypes(urlConnection);

            if (parsedTypes.isEmpty()) {
                return makeNegativeResponse();
            }

            return CompletableFuture.completedFuture(returnsHTMLDocument(parsedTypes));
        } catch (final SecurityException | IOException exception) {
            LOGGER.error("Unable to check URI '" + uri + "'. Skipping further processing.", exception);
        }
        return makeNegativeResponse();
    }
}
