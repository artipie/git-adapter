package com.artipie.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import com.artipie.ArtipieException;
import com.artipie.asto.Content;
import com.artipie.asto.Copy;
import com.artipie.asto.Storage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsWithBody;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.reactivestreams.Publisher;

final class LsRefsSlice implements Slice {
    
    private final Storage storage;

    LsRefsSlice(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public Response response(String line, Iterable<Entry<String, String>> headers, Publisher<ByteBuffer> body) {
        Path tmp;
        try {
            tmp = Files.createTempDirectory(LsRefsSlice.class.getName());
        } catch (IOException e) {
            throw new ArtipieException(e);
        }
        return new AsyncResponse(
            new TmpResource(tmp).<Response>with(
                tsto -> new Copy(this.storage).copy(tsto).thenApply(
                    none -> {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            final Repository repo = new FileRepository(tmp.toAbsolutePath().toFile());
                            ObjectId head = repo.resolve("HEAD");
                            if (head == null) {
                                repo.create(true);
                                head = repo.resolve("HEAD");
                            }
                            final PacketLineOut out = new PacketLineOut(baos);
                            final RefAdvertiser adv = new RefAdvertiser.PacketLineOutRefAdvertiser(out);
                            adv.init(repo);
                            adv.setDerefTags(true);
                            adv.send(repo.getRefDatabase().getRefsByPrefix("refs/"));
                            out.end();
                        } catch (final IOException iex) {
                            throw new ArtipieException(iex);
                        }
                        return new Content.From(baos.toByteArray());
                    }
                ).thenApply(RsWithBody::new)
            )
        );
    }
}
