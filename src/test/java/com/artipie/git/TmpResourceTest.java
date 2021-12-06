/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;
import org.testcontainers.shaded.org.hamcrest.Matchers;

/**
 * Test case for {@link TmpResource}.
 *
 * @since 1.0
 */
final class TmpResourceTest {

    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void removeFilesOnCleanup(@TempDir final Path tmp) throws Exception {
        final Path target = tmp.resolve("test-1");
        Files.createDirectory(target);
        new TmpResource(target)
            .with(
                st -> CompletableFuture.allOf(
                    st.save(new Key.From("one"), new Content.From("1".getBytes())),
                    st.save(new Key.From("dir", "two"), new Content.From("2".getBytes())),
                    st.save(new Key.From("dir", "three"), new Content.From("3".getBytes())),
                    st.save(new Key.From("four"), new Content.From("4".getBytes()))
                )
            ).join();
        MatcherAssert.assertThat(
            Files.exists(target),
            Matchers.is(false)
        );
    }
}
