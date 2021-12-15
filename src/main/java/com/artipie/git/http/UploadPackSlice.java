/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.http;

import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.git.sdk.GitRequest;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rt.RtRule;
import com.artipie.http.slice.SliceSimple;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.reactivestreams.Publisher;

/**
 * Slice to handle {@code upload-pack} service commands to support {@code git fetch}
 * commands.
 * <p>
 * {@code upload-pack} service is initiated by client's {@code fetch-pack} command which
 * is triggered by {@code git fetch} to receive data from server. Fetching pack consists
 * of two phases:
 * <ol>
 *   <li>{@code GET} HTTP request to {@code upload-pack} service, where client is sending
 *   own references and expects new updates to calculate the difference. Also, some server
 *   metadata is included into the response</li>
 *   <li>{@code POST} HTTP request to {@code upload-pack} service, where client sends
 *   references to include into pack file, and server uploads pack to client.</li>
 * </ol>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.LongVariable")
final class UploadPackSlice implements Slice {

    /**
     * Service routing name.
     */
    static final RtRule RT_RULE = new GitSlice.ByService("git-upload-pack");

    /**
     * Error slice for command not found.
     */
    private static final Slice SLICE_CMD_NOT_FOUND = new SliceSimple(
        new RsWithBody(
            new RsWithStatus(RsStatus.BAD_REQUEST),
            "command not found", StandardCharsets.US_ASCII
        )
    );

    /**
     * Error slice for bad request response.
     */
    private static final Slice SLICE_BAD_REQUEST = new SliceSimple(
        new RsWithStatus(RsStatus.BAD_REQUEST)
    );

    /**
     * Commands mapping.
     */
    private final Map<String, Slice> commands;

    /**
     * New upload pack service.
     * @param storage Repository storage
     */
    UploadPackSlice(final Storage storage) {
        this.commands = buildCommands(storage);
    }

    @Override
    public Response response(final String line, final Iterable<Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final Publisher<ByteBuffer> cache = Flowable.fromPublisher(body).cache();
        return new AsyncResponse(
            new PublisherAs(cache).asciiString().thenApply(GitRequest::parse).thenApply(
                req -> req.command().map(
                    name -> this.commands.getOrDefault(name, UploadPackSlice.SLICE_CMD_NOT_FOUND)
                ).orElse(UploadPackSlice.SLICE_BAD_REQUEST)
            ).thenApply(slice -> slice.response(line, headers, body))
        );
    }

    /**
     * Build upload-pack service command slices.
     *
     * @param storage Repository storage
     * @return Mapping of command names to slice objects
     */
    private static Map<String, Slice> buildCommands(final Storage storage) {
        final Map<String, Slice> map = new HashMap<>(1);
        map.put("ls-refs", new LsRefsSlice(storage));
        return Collections.unmodifiableMap(map);
    }
}
