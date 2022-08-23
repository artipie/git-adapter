/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.http;

import com.artipie.asto.Content;
import com.artipie.asto.Storage;
import com.artipie.git.sdk.Git;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsWithBody;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import org.reactivestreams.Publisher;

/**
 * Slice to handle {@code ls-refs} command.
 *
 * @since 1.0
 */
final class LsRefsSlice implements Slice {

    /**
     * Repository storage.
     */
    private final Storage storage;

    /**
     * New slice.
     *
     * @param storage Repo storage
     */
    LsRefsSlice(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public Response response(final String line, final Iterable<Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new AsyncResponse(
            new Git(this.storage).lsRefs(baos)
                .thenApply(none -> new RsWithBody(new Content.From(baos.toByteArray())))
        );
    }
}
