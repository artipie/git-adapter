/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.http.ArtipieHttpException;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rt.RtRule;
import com.artipie.http.slice.SliceSimple;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;

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
final class UploadPackSlice implements Slice {

    /**
     * Service routing name.
     */
    static final RtRule RT_RULE = new GitSlice.ByService("git-upload-pack");

    private final Storage storage;

    /**
     * New upload pack service.
     */
    UploadPackSlice(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public Response response(String rline, Iterable<Entry<String, String>> headers, Publisher<ByteBuffer> body) {
        Publisher<ByteBuffer> cache = Flowable.fromPublisher(body).cache();
        return new AsyncResponse(
            new PublisherAs(cache).asciiString().thenApply(
                raw -> GitRequest.parse(raw)
            ).thenApply(
                greq -> {
                    final String command = greq.command().orElseThrow(() -> new ArtipieHttpException(RsStatus.BAD_REQUEST));
                    if (command.equals("ls-refs")) {
                        return new LsRefsSlice(this.storage).response(rline, headers, cache);
                    } else {
                        return new RsWithStatus(RsStatus.NOT_FOUND);
                    }
                }
            )
        );
    }
}
