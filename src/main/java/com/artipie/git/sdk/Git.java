/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.sdk;

import com.artipie.asto.ArtipieIOException;
import com.artipie.asto.Copy;
import com.artipie.asto.Storage;
import com.artipie.asto.misc.UncheckedSupplier;
import com.artipie.git.TmpResource;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser;

/**
 * Git SDK.
 * @since 1.0
 * @checkstyle MethodBodyCommentsCheck (500 lines)
 */
public final class Git {

    /**
     * Repository storage.
     */
    private final Storage storage;

    /**
     * New git SDK.
     * @param storage Repository storage
     */
    public Git(final Storage storage) {
        this.storage = storage;
    }

    /**
     * Perform ls-refs command on a git reposirory.
     * @param out Output stream for response
     * @return Status future
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public CompletableFuture<? extends Void> lsRefs(final OutputStream out) {
        return CompletableFuture.supplyAsync(
            new UncheckedSupplier<>(() -> Files.createTempDirectory(Git.class.getName()))
        ).thenCompose(
            tmp -> new TmpResource(tmp).<Void>with(
                tsto -> new Copy(this.storage).copy(tsto).<Void>thenApply(
                    none -> {
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
                            final PacketLineOut plout = new PacketLineOut(out);
                            final RefAdvertiser adv =
                                new RefAdvertiser.PacketLineOutRefAdvertiser(plout);
                            adv.init(repo);
                            adv.setDerefTags(true);
                            adv.send(repo.getRefDatabase().getRefsByPrefix("HEAD"));
                            adv.send(repo.getRefDatabase().getRefsByPrefix("refs/"));
                            plout.end();
                        } catch (final IOException iex) {
                            throw new ArtipieIOException(iex);
                        }
                        return (Void) null;
                    }
                )
            )
        );
    }
}
