package com.example.alva.visitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

final class UrlFilterChain {

    private static final String LEGAL_HYPERLINK_CHARS = "[\\w\\?\\:\\/\\#\\-\\&\\=\\.\\;\\(\\)\\%\\,\\–\\p{L}]";
    private static final String GROUP_NAME = "hyperlinkValue";
    private static final Pattern NO_FOLLOW = Pattern.compile("rel=\\\"nofollow\\\"");
    private static final Pattern HREF =
        Pattern.compile("href=\\\"(?<" + GROUP_NAME + ">" + LEGAL_HYPERLINK_CHARS + "*)\\\"");
    private final URI baseURI;

    UrlFilterChain(final URI baseURI) {
        this.baseURI = baseURI;
    }

    private static boolean isStartingAnchorTag(final String line) {
        return line.startsWith("<a ");
    }

    private static boolean containsNoFollowRelation(final String line) {
        return NO_FOLLOW.matcher(line).find();
    }

    private static Optional<String> extractHyperlinkValue(final String line) {
        final Matcher matcher = HREF.matcher(line);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(GROUP_NAME));
    }

    private static boolean isNotInternalAnchor(final String hyperlink) {
        final boolean isInternalAnchor = hyperlink.startsWith("#");
        return !isInternalAnchor;
    }

    private static Optional<URI> transformHyperlinkToAbsoluteURI(final URI baseURI, final String hyperlink) {
        try {
            final URI uri = new URI(hyperlink);

            if (!uri.isAbsolute()) {
                final URI absolute = baseURI.resolve(uri);
                return Optional.of(absolute);
            }

            return Optional.of(uri);
        } catch (final URISyntaxException e) {
            return Optional.empty();
        }
    }

    private static URI discardFragments(final URI uri) {
        if (uri.getFragment() != null) {
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                    uri.getQuery(), null);
            } catch (final URISyntaxException e) {
                // igored
            }
        }

        return uri;
    }

    Queue<Pair<URI, Integer>> filterChildURLs(final Stream<String> lines) {

        return lines
            .parallel()
            .filter(UrlFilterChain::isStartingAnchorTag)
            .filter(line -> !containsNoFollowRelation(line))
            .map(UrlFilterChain::extractHyperlinkValue)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(UrlFilterChain::isNotInternalAnchor)
            .map(hyperlink -> transformHyperlinkToAbsoluteURI(this.baseURI, hyperlink))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(UrlFilterChain::discardFragments)
            .collect(Collectors.toMap(Function.identity(), url -> 1, (integer, integer2) -> integer + integer2))
            .entrySet()
            .parallelStream()
            .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
            .collect(Collectors.toCollection(LinkedBlockingQueue::new));
    }
}
