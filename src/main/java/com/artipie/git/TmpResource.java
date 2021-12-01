package com.artipie.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.artipie.asto.Storage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.misc.UncheckedSupplier;


final class TmpResource {
    
    final Supplier<? extends Path> factory;

    public TmpResource(final Path path) {
        this.factory = () -> path;
    }

    public TmpResource() {
        this.factory = new UncheckedSupplier<>(() -> Files.createTempDirectory(TmpResource.class.getCanonicalName()));
    }

    <T> CompletableFuture<? extends T> with(Function<? super Storage, ? extends CompletionStage<T>> func) {
        return CompletableFuture.supplyAsync(this.factory)
            .thenCompose((Path path) -> func.apply(new FileStorage(path)).handle(TmpResource.removeTmp(path)));
    }

    private static <T> BiFunction<? super T, Throwable, T> removeTmp(final Path path) {
        return (T res, Throwable thr) -> {
            Throwable err = thr;
            try {
                for (final Path child : Files.walk(path).collect(Collectors.toList())) {
                        Files.delete(child);
                }
                Files.delete(path);
            } catch (IOException iex) {
                if (err != null) {
                    err.addSuppressed(iex);
                } else {
                    err = iex;
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
