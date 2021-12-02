package com.artipie.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.artipie.ArtipieException;

import org.apache.commons.codec.binary.Hex;

/**
 * Git request data accessor.
 * @since 1.0
 */
final class GitRequest {
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
    Optional<String> command() {
        return lines.stream().filter(line -> line.startsWith("command="))
            .map(line -> line.substring(8)).findFirst().map(String::trim);
    }

    /**
     * Parsed parts.
     * @return Immutable list
     */
    List<String> parts() {
        return Collections.unmodifiableList(this.lines);
    }

    /**
     * Parse raw request data.
     * @param raw Request data
     * @return Request data accessor
     */
    static GitRequest parse(final String raw) {
        String rest = raw;
        final List<String> lines = new ArrayList<>();
        while (!rest.isEmpty()) {
            if (rest.length() < 4) {
                throw new ArtipieException("Invalid git request line lengh");
            }
            final int len = Integer.parseInt(rest.substring(0, 4), 16);
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
