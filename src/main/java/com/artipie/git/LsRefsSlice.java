/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import com.artipie.asto.ArtipieIOException;
import com.artipie.asto.Content;
import com.artipie.asto.Copy;
import com.artipie.asto.Storage;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsWithBody;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.reactivestreams.Publisher;

/**
 * Slice to handle {@code ls-refs} command.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MethodBodyCommentsCheck (500 lines)
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
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Response response(final String line, final Iterable<Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final Path tmp;
        try {
            tmp = Files.createTempDirectory(LsRefsSlice.class.getName());
        } catch (final IOException iex) {
            throw new ArtipieIOException(iex);
        }
        return new AsyncResponse(
            new TmpResource(tmp).<Response>with(
                tsto -> new Copy(this.storage).copy(tsto).thenApply(
                    none -> {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            final Repository repo = new FileRepository(
                                tmp.toAbsolutePath().toFile()
                            );
                            // every bare repository must have `HEAD` reference:
                            // no `HEAD` means that repository doesn't exist here,
                            // and we should create it.
                            if (repo.resolve("HEAD") == null) {
                                repo.create(true);
                            }
                            final PacketLineOut out = new PacketLineOut(baos);
                            final RefAdvertiser adv =
                                new RefAdvertiser.PacketLineOutRefAdvertiser(out);
                            adv.init(repo);
                            adv.setDerefTags(true);
                            adv.send(repo.getRefDatabase().getRefsByPrefix("HEAD"));
                            adv.send(repo.getRefDatabase().getRefsByPrefix("refs/"));
                            out.end();
                        } catch (final IOException iex) {
                            throw new ArtipieIOException(iex);
                        }
                        return new Content.From(baos.toByteArray());
                    }
                ).thenApply(RsWithBody::new)
            )
        );
    }
}
