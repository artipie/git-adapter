/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import com.artipie.asto.Storage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.misc.UncheckedSupplier;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Temporary storage as resource.
 *
 * @since 1.0
 */
public final class TmpResource {

    /**
     * Tmp path factory.
     */
    private final Supplier<? extends Path> factory;

    /**
     * New tmp resource with fixed path.
     *
     * @param path Tmp path
     */
    public TmpResource(final Path path) {
        this(() -> path);
    }

    /**
     * New tmp resource with random path.
     */
    public TmpResource() {
        this(
            new UncheckedSupplier<>(
                () -> Files.createTempDirectory(TmpResource.class.getCanonicalName())
            )
        );
    }

    /**
     * Primary constructor.
     * @param factory Tmp path factory
     */
    public TmpResource(final Supplier<? extends Path> factory) {
        this.factory = factory;
    }

    /**
     * Perform operations with this resource and cleanup after that.
     *
     * @param func Operation function
     * @param <T> Result type
     * @return Operation result
     */
    public <T> CompletableFuture<? extends T> with(
        final Function<? super Storage, ? extends CompletionStage<T>> func
    ) {
        return CompletableFuture.supplyAsync(this.factory).thenCompose(
            (Path path) -> func.apply(new FileStorage(path)).handle(TmpResource.cleanup(path))
        );
    }

    /**
     * Cleanup function.
     *
     * @param path Path to cleanup
     * @param <T> Return type
     * @return Cleanup function
     */
    private static <T> BiFunction<? super T, Throwable, T> cleanup(final Path path) {
        return (T res, Throwable thr) -> {
            Throwable err = thr;
            try {
                Files.walkFileTree(
                    path,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(final Path file,
                            final BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(final Path dir,
                            final IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    }
                );
            } catch (final IOException iex) {
                if (err == null) {
                    err = iex;
                } else {
                    err.addSuppressed(iex);
                }
            }
            if (err != null) {
                if (err instanceof RuntimeException) {
                    throw (RuntimeException) err;
                } else {
                    throw new CompletionException(err);
                }
            }
            return res;
        };
    }
}
