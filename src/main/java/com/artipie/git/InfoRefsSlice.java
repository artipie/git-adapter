/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import com.artipie.asto.ArtipieIOException;
import com.artipie.asto.Content;
import com.artipie.http.ArtipieHttpException;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.headers.ContentType;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqParams;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.StandardRs;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.Set;
import org.reactivestreams.Publisher;

/**
 * Slice to handle {@code /info/refs} - it's used
 * to send metadata about git server, it shows supported commands
 * services, versions, etc.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MethodBodyCommentsCheck (500 lines)
 */
final class InfoRefsSlice implements Slice {

    /**
     * Server agent name.
     */
    private final String agent;

    /**
     * Supported commands.
     */
    private final Set<String> commands;

    /**
     * New slice.
     * @param agent Server agent name
     * @param commands Supported commands with annotations
     */
    InfoRefsSlice(final String agent, final Set<String> commands) {
        this.agent = agent;
        this.commands = commands;
    }

    @Override
    public Response response(final String line, final Iterable<Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final String service = new RqParams(new RequestLineFrom(line).uri()).value("service")
            .orElseThrow(
                () -> new ArtipieHttpException(
                    RsStatus.BAD_REQUEST,
                    "service query param required"
                )
            );
        // this response is very small: <1K - it doesn't consume a lot of memory
        // and it could be constructed in byte-array right here
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        try (OutputStreamWriter osw = new OutputStreamWriter(baos)) {
            final GitResponseOutput gwr = new GitResponseOutput(osw);
            gwr.pushLine(String.format("# service=%s", service));
            gwr.endPart();
            gwr.pushLine("version 2");
            gwr.pushLine(String.format("agent=%s", this.agent));
            for (final String cmd : this.commands) {
                gwr.pushLine(cmd);
            }
            gwr.pushLine("server-option");
            gwr.pushLine("object-format=sha1");
            gwr.endPart();
        } catch (final IOException iex) {
            throw new ArtipieHttpException(RsStatus.INTERNAL_ERROR, new ArtipieIOException(iex));
        }
        return new RsWithBody(
            new RsWithHeaders(
                StandardRs.OK,
                new ContentType("application/x-git-upload-pack-advertisement")
            ),
            new Content.From(baos.toByteArray())
        );
    }
}
