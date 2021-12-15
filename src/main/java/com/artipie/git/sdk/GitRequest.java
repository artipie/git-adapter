/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.sdk;

import com.artipie.ArtipieException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Git request data accessor.
 * @since 1.0
 * @checkstyle MagicNumberCheck (300 lines)
 * @checkstyle MethodBodyCommentsCheck (300 lines)
 */
public final class GitRequest {
    /**
     * Request lines.
     */
    private final List<String> lines;

    /**
     * New git request.
     * <p>
     * Use {@link #parse(String)} to parse raw request data.
     * </p>
     * @param lines Request lines
     */
    GitRequest(final List<String> lines) {
        this.lines = lines;
    }

    /**
     * Parse command.
     * @return Command if found
     */
    public Optional<String> command() {
        return this.lines.stream().filter(line -> line.startsWith("command="))
            .map(line -> line.substring(8)).findFirst().map(String::trim);
    }

    /**
     * Parsed parts.
     * @return Immutable list
     */
    public List<String> parts() {
        return Collections.unmodifiableList(this.lines);
    }

    /**
     * Parse raw request data.
     * @param raw Request data
     * @return Request data accessor
     * @throws ArtipieException In case of request is invalid
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static GitRequest parse(final String raw) {
        String rest = raw;
        final List<String> lines = new ArrayList<>(10);
        while (!rest.isEmpty()) {
            if (rest.length() < 4) {
                throw new ArtipieException("Invalid git request line lengh");
            }
            // parse line prefix (first 4 ASCI chars as hex digits) to integer
            final int len = Integer.parseInt(rest.substring(0, 4), 16);
            // sometimes git request contain length smaller than 4 instead of just skipping it
            if (len < 4) {
                rest = rest.substring(4);
                continue;
            }
            final String line = rest.substring(4, len);
            lines.add(line);
            rest = rest.substring(len);
        }
        return new GitRequest(lines);
    }
}
