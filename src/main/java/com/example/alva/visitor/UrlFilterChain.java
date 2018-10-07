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

import com.example.alva.analytics.AnchorIssue;
import com.example.alva.analytics.HyperlinkIssue;
import com.example.alva.analytics.LinkVisitorAnalytics;
import com.example.alva.analytics.TransformationSuccess;

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

    private static boolean containsNoFollowRelation(final String line, final LinkVisitorAnalytics analytics) {
        final boolean matches = NO_FOLLOW.matcher(line).find();
        if (matches) {
            analytics.reportIgnoredAnchor(line, AnchorIssue.NO_FOLLOW_RELATION);
        }
        return matches;
    }

    private static Optional<String> extractHyperlinkValue(final String line, final LinkVisitorAnalytics analytics) {
        final Matcher matcher = HREF.matcher(line);

        if (!matcher.find()) {
            analytics.reportIgnoredAnchor(line, AnchorIssue.NO_HYPERLINK_FOUND);
            return Optional.empty();
        }

        return Optional.of(matcher.group(GROUP_NAME));
    }

    private static boolean isNotInternalAnchor(final String hyperlink, final LinkVisitorAnalytics analytics) {
        final boolean isInternalAnchor = hyperlink.startsWith("#");
        if (isInternalAnchor) {
            analytics.reportIgnoredAnchor(hyperlink, AnchorIssue.IS_INTERNAL_ANCHOR);
        }
        return !isInternalAnchor;
    }

    private static Optional<URI> transformHyperlinkToAbsoluteURI(final URI baseURI, final String hyperlink,
        final LinkVisitorAnalytics analytics) {
        try {
            final URI uri = new URI(hyperlink);

            if (!uri.isAbsolute()) {
                final URI absolute = baseURI.resolve(uri);
                analytics.reportTransformedHyperlink(hyperlink, absolute.toString(),
                    TransformationSuccess.TEXTUAL_HYPERLINK_TO_ABSOLUTE_URI);
                return Optional.of(absolute);
            }

            analytics.reportIgnoredHyperlink(hyperlink,
                HyperlinkIssue.TEXTUAL_HYPERLINK_TO_ABSOLUTE_URI_CONVERSION_SKIPPED);
            return Optional.of(uri);
        } catch (final URISyntaxException e) {
            analytics.reportIgnoredHyperlink(hyperlink, HyperlinkIssue.TEXTUAL_HYPERLINK_CANNOT_BE_PARSED);
            return Optional.empty();
        }
    }

    private static URI discardFragments(final URI uri, final LinkVisitorAnalytics analytics) {
        if (uri.getFragment() != null) {
            try {
                final URI shortenedURI =
                    new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                        uri.getQuery(), null);

                analytics.reportTransformedHyperlink(uri.toString(), shortenedURI.toString(),
                    TransformationSuccess.DISCARDING_FRAGMENT);
                return shortenedURI;
            } catch (final URISyntaxException e) {
                analytics.reportIgnoredHyperlink(uri.toString(), HyperlinkIssue.DISCARDING_FRAGMENT_FAILED);
            }
        }

        return uri;
    }

    Queue<Pair<URI, Integer>> filterChildURLs(final LinkVisitorAnalytics linkVisitorAnalytics,
        final Stream<String> lines) {

        return lines
            .parallel()
            .filter(UrlFilterChain::isStartingAnchorTag)
            .filter(line -> !containsNoFollowRelation(line, linkVisitorAnalytics))
            .map(line -> extractHyperlinkValue(line, linkVisitorAnalytics))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(hyperlink -> isNotInternalAnchor(hyperlink, linkVisitorAnalytics))
            .map(hyperlink -> transformHyperlinkToAbsoluteURI(this.baseURI, hyperlink, linkVisitorAnalytics))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(uri -> discardFragments(uri, linkVisitorAnalytics))
            .collect(Collectors.toMap(Function.identity(), url -> 1, (integer, integer2) -> integer + integer2))
            .entrySet()
            .parallelStream()
            .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
            .collect(Collectors.toCollection(LinkedBlockingQueue::new));
    }
}
