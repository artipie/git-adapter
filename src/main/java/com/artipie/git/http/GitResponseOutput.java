/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.http;

import java.io.IOException;
import java.io.Writer;

/**
 * Git response formatter writes binary data to
 * wrapped {@link Writer} with correct identations and
 * line prefixes.
 * <p>
 * Git response data consists of any number of part, each part
 * ends with empty line (no data binary line).<br/>
 * Git data line contains ASCI characters.<br/>
 * Each data line is prefixed with 4-byte length of full line in hex ASCI
 * format, e.g. text hello will be printed as
 * {@code `000ahello\n`: `000a = hex(4 + len("hello") + 1)}<br/>
 * Each part ends with empty data line: {@code 0000}.
 * @since 1.0
 */
final class GitResponseOutput {

    /**
     * End part symbols.
     */
    private static final char[] END_PART = new char[]{'0', '0', '0', '0'};

    /**
     * Output writer.
     */
    private final Writer out;

    /**
     * New git output.
     * @param writer Writer
     */
    GitResponseOutput(final Writer writer) {
        this.out = writer;
    }

    /**
     * Push new line to output, format it as git data line.
     * @param line ASCI text line
     * @throws IOException On IO error
     * @checkstyle MagicNumberCheck (10 lines)
     */
    void pushLine(final String line) throws IOException {
        final char[] src = line.toCharArray();
        final char[] res = new char[src.length + 5];
        final char[] len = String.format("%04x", res.length).toCharArray();
        System.arraycopy(len, 0, res, 0, len.length);
        System.arraycopy(src, 0, res, len.length, src.length);
        res[res.length - 1] = '\n';
        this.out.write(res);
    }

    /**
     * Write end part symbol.
     * @throws IOException Of IO error
     */
    void endPart() throws IOException {
        this.out.write(GitResponseOutput.END_PART);
    }
}
