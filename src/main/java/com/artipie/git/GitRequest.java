package com.artipie.git;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.artipie.ArtipieException;

import org.apache.commons.codec.binary.Hex;

final class GitRequest {
    private final List<String> lines;

    GitRequest(final List<String> lines) {
        this.lines = lines;
    }

    Optional<String> command() {
        return lines.stream().filter(line -> line.startsWith("command="))
            .map(line -> line.substring(8)).findFirst();
    }

    static GitRequest parse(final String raw) {
        final List<String> lines = new ArrayList<>();
        for (final String line : raw.split("\n")) {
            if (line.length() < 4) {
                throw new ArtipieException("Invalid git request line lengh");
            }
            final int len = Integer.parseInt(line.substring(0, 4), 16);
            if (len == 0) {
                continue;
            }
            final String rest = line.substring(4);
            if (rest.length() != len - 5) {
                throw new ArtipieException("Invalid line, verification failed");
            }
        }
        return new GitRequest(lines);
    }
}
