/*
 * The MIT License (MIT) Copyright (c) 2020-2024 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.http;

import com.artipie.asto.misc.UncheckedConsumer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link GitResponseOutput}.
 * @since 1.0
 */
final class GitResponseOutputTest {
    @Test
    void writeDataLine() {
        MatcherAssert.assertThat(
            withGitOutput(
                new UncheckedConsumer<>(
                    out -> out.pushLine("hello")
                )
            ),
            new IsEqual<>("000ahello\n")
        );
    }

    @Test
    void writeParts() {
        MatcherAssert.assertThat(
            withGitOutput(
                new UncheckedConsumer<>(
                    out -> out.endPart()
                )
            ),
            new IsEqual<>("0000")
        );
    }

    @Test
    void writeMultipleParts() {
        MatcherAssert.assertThat(
            withGitOutput(
                new UncheckedConsumer<>(
                    out -> {
                        out.pushLine("line-1");
                        out.endPart();
                        out.pushLine("line-02");
                        out.pushLine("line-003");
                        out.endPart();
                    }
                )
            ),
            new IsEqual<>("000bline-1\n0000000cline-02\n000dline-003\n0000")
        );
    }

    /**
     * Perform operation with git data output and return ASCI string with result.
     * @param func Consumer
     * @return ASCI string
     */
    private static String withGitOutput(final Consumer<? super GitResponseOutput> func) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OutputStreamWriter osw = new OutputStreamWriter(baos);
        func.accept(new GitResponseOutput(osw));
        try {
            osw.flush();
        } catch (final IOException iex) {
            throw new UncheckedIOException(iex);
        }
        return new String(baos.toByteArray(), StandardCharsets.US_ASCII);
    }
}
