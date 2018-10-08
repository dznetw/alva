package com.example.alva.visitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Objects;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.example.alva.storage.VisitorProcess;
import com.google.common.base.Splitter;

public class LinkVisitor {

    private VisitorProcess process;

    public LinkVisitor(final VisitorProcess process) {
        this.process = Objects.requireNonNull(process);
    }

    public Queue<Pair<URI, Integer>> visit() throws IOException {
        try (final InputStreamReader reader = new InputStreamReader(this.process.getBaseURL().openStream());
            final BufferedReader bufferedReader = new BufferedReader(reader)) {

            final Stream<String> lines = ReFormatter.reformatHTMLWithTagOnEachNewLine(bufferedReader);

            return new UrlFilterChain(this.process.getBaseURI()).filterChildURLs(lines);
        }
    }

    private static final class ReFormatter {

        private static final Splitter NEW_LINE_SPLITTER = Splitter.on("\n");
        private static final Pattern TAG_START = Pattern.compile("\\<");
        private static final String TAG_START_REPLACEMENT = "\n\\<";

        private static String insertingLineBreaksInFrontOfAllAngledBrackets(final String htmlContent) {
            return TAG_START.matcher(htmlContent).replaceAll(TAG_START_REPLACEMENT);
        }

        private static Stream<String> reformatHTMLWithTagOnEachNewLine(final BufferedReader bufferedReader) {
            // utilizing BufferedReader implementation that removes line breaks, and joining complete HTML to single
            // character string without format
            final String htmlContent = bufferedReader.lines().collect(Collectors.joining());
            final String reformatted = insertingLineBreaksInFrontOfAllAngledBrackets(htmlContent);
            return NEW_LINE_SPLITTER.splitToList(reformatted).stream();
        }
    }
}
